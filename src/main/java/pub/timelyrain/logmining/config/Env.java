package pub.timelyrain.logmining.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import java.util.Arrays;

@Configuration
@ConfigurationProperties(prefix = "mining")
public class Env {

    private String exchangeName;
    private String queueName;
    private Integer interval;

    private boolean multiTenant;

    private String[] tables;

    public String[] getTables() {
        return tables;
    }

    public void setTables(String[] tables) {
        this.tables = tables;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    public boolean isMultiTenant() {
        return multiTenant;
    }

    public void setMultiTenant(boolean multiTenant) {
        this.multiTenant = multiTenant;
    }


    @Override
    public String toString() {
        return "Env{" +
                "exchangeName='" + exchangeName + '\'' +
                ", queueName='" + queueName + '\'' +
                ", interval=" + interval +
                ", multiTenant=" + multiTenant +
                ", tables=" + Arrays.toString(tables) +
                '}';
    }

    public void startCheck(){
        Assert.notNull(tables,"需要设置同步表 mining.tables ");
    }
}
