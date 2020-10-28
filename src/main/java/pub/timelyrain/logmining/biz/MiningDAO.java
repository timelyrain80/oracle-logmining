package pub.timelyrain.logmining.biz;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import pub.timelyrain.logmining.pojo.Row;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class MiningDAO {
    Logger log = LogManager.getLogger(MiningDAO.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private SQLExtractor sqlExtractor;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${mining.sync-table}")
    private String tables;
    @Value("${mining.polling-interval}")
    private int pollingInterval;
    @Value("${mining.polling-size}")
    private int pollingSize;
    @Value("${mining.init-start-scn}")
    private int initStartScn;
    @Value("${mining.mq-exchage-name}")
    private String mqExchangeName;

    private String miningSql;

    public void startMining() {
        //检查
        try {

            long startScn = loadStartScn();
            Assert.hasText(tables, "同步表名不能为空");
            Assert.isTrue(tables.contains("."), "同步表名需要为 数据库名.表名 的格式");
            long currentScn = loadDatabaseCurrentScn();
            Assert.isTrue(startScn <= currentScn, String.format("起始SCN %d 大于 当前SCN %d,请检查起始SCN配置", startScn, currentScn));

            miningSql = buildMiningSql();

            while (true) {
                long endScn = polling(startScn);
                startScn = endScn;

                saveEndScn(endScn);
            }

        } catch (Exception e) {
            log.error("系统运行错误", e);
        }

    }

    private void saveEndScn(long endScn) throws IOException {
        File state = new File("state.saved");
        FileUtils.writeStringToFile(state, String.valueOf(endScn), "UTF-8", false);
        log.info("save state to {}", state.getPath());
    }

    private long loadStartScn() throws IOException {
        if (initStartScn != 0) {  //如果启动时设置了起始scn，则优先使用
            log.info("指定了初始抓取scn {}, 从此处开始获取数据", initStartScn);
            return initStartScn;
        }
        File state = new File("state.saved");

        if (!state.exists()) {
            long scn = loadDatabaseCurrentScn(); //不存在使用数据库当前scn
            log.info("未找到状态文件，从当前scn开始 {}", scn);
            return scn;
        }
        //读取配置文件的scn
        String scn = FileUtils.readFileToString(state, "utf-8");
        log.info("从状态文件中读取上次执行的scn末位,({} -> {})", scn, state.getPath());
        return Long.parseLong(scn);

    }

    private long loadDatabaseCurrentScn() {
        return jdbcTemplate.queryForObject(Constants.QUERY_CURRENT_SCN, Long.class);
    }

    private long polling(long startScn) throws InterruptedException {
        //查询数据库当前scn
        long currentScn = loadDatabaseCurrentScn();

        if (startScn == currentScn) {
            String msg = String.format("已捕获至日志末尾,暂停 %d 秒. (起始SCN %d 等于 当前SCN %d)", pollingInterval, startScn, currentScn);
            TimeUnit.SECONDS.sleep(pollingInterval);
        }
        //计算分析一次归档的终点scn，性能调优参数，在一次分析的范围大小与数据库压力之间调整，
        //endScn不应大于数据库最大scn值
        long endScn = Math.min(startScn + pollingSize, currentScn);

        //启动日志分析
        log.debug("开始分析REDO日志 " + Constants.MINING_START.replaceAll("\\?", "{}"), startScn, endScn);
        jdbcTemplate.update(Constants.MINING_START, startScn, endScn);
        jdbcTemplate.setFetchSize(1000);

        log.debug("提取REDO日志 {}", miningSql);
        jdbcTemplate.query(miningSql, (rs) -> {
            //计数
            Counter.addCount();
            //读取REDO
            long scn = rs.getLong("scn");
            int csf = rs.getInt("CSF");
            String schema = rs.getString("");
            String tableName = rs.getString("");
            String redo = rs.getString("SQL_REDO");
            if (csf == 1) {     //如果REDO被截断
                while (rs.next()) {  //继续查询下一条REDO
                    redo += rs.getString("SQL_REDO");
                    if (0 == rs.getInt("CSF")) {  //
                        csf = 0;
                        break;  //退出循环
                    }
                }
            }
            if (csf == 0) {
                convertAndDelivery(schema, tableName, redo);
            } else {
                log.warn("REDO log 处于截断状态 SCN:{} ,REDO SQL {}", scn, redo);
            }
        });

        //关闭日志分析
        log.debug("停止分析REDO日志 {}", Constants.MINING_END);
        jdbcTemplate.update(Constants.MINING_END);
        return endScn;  //返回本次结束的scn，作为下一次轮询的startScn
    }

    private String buildMiningSql() {
        tables = tables.toUpperCase();
        String[] tbs = tables.split(",");
        HashMap<String, String> tableGroup = new HashMap<>();
        //将数据库名.表名的数组，转成 数据库名和表名的集合
        for (String tb : tbs) {
            Assert.isTrue(tb.contains("."), "同步表名格式不符合 数据库名.表名 的格式(" + tb + ")");
            String schemaName = tb.split("\\.")[0]; // 数据库名.表名
            String tableName = tb.split("\\.")[1]; // 数据库名.表名
            String tablesIn = tableGroup.get(schemaName);
            if (tableName.contains("*")) {
                log.debug("{}设置为捕获全部表.", schemaName);
                tablesIn = "*";
            } else {
                tablesIn = (tablesIn == null ? "'" + tableName + "'" : tablesIn + ",'" + tableName + "'");
                log.debug("拼装同步表名的过滤sql {}->{}", schemaName, tablesIn);
            }
            tableGroup.put(schemaName, tablesIn);
        }

        StringBuilder sql = new StringBuilder();

        for (String schemaName : tableGroup.keySet()) {
            if (sql.length() != 0) sql.append(" or ");

            String tablesIn = tableGroup.get(schemaName);
            if (tablesIn.startsWith("*"))
                sql.append(String.format(" SEG_OWNER = '%s' ", schemaName));
            else
                sql.append(String.format(" SEG_OWNER = '%s' and TABLE_NAME in (%s)", schemaName, tableGroup.get(schemaName)));
        }
        sql.insert(0, " AND ( ").append(" )");
        sql.insert(0, Constants.QUERY_REDO);
        log.debug("日志查询的sql是 {}", sql.toString());
        return sql.toString();
    }

    private void convertAndDelivery(String schema, String tableName, String redo) {
        try {
            Row row = sqlExtractor.parse(schema, tableName, redo);
            rabbitTemplate.convertAndSend(mqExchangeName, schema + "." + tableName, row);

        } catch (Exception e) {
            log.error("转换REDO和分发数据错误", e);
        }
    }


}
