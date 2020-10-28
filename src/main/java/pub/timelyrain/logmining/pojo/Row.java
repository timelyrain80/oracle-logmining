package pub.timelyrain.logmining.pojo;

import java.util.List;
import java.util.Map;

public class Row {
    private String schemaName;
    private String tableName;
    /**
     * DML,DDL
     */
    private String mode;
    private String Sql;

    /**
     * INSERT,UPDATE,DELETE
     */
    private String operator;
    private Map<String, String> newData;
    private Map<String, String> oldData;
    private List structure;

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getSql() {
        return Sql;
    }

    public void setSql(String sql) {
        Sql = sql;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Map<String, String> getNewData() {
        return newData;
    }

    public void setNewData(Map<String, String> newData) {
        this.newData = newData;
    }

    public Map<String, String> getOldData() {
        return oldData;
    }

    public void setOldData(Map<String, String> oldData) {
        this.oldData = oldData;
    }

    public List getStructure() {
        return structure;
    }

    public void setStructure(List structure) {
        this.structure = structure;
    }
}