package pub.timelyrain.logmining.biz;

public class Constants {
    public static final String MINING_START = "begin SYS.DBMS_LOGMNR.START_LOGMNR(OPTIONS => SYS.DBMS_LOGMNR.SKIP_CORRUPTION+SYS.DBMS_LOGMNR.NO_SQL_DELIMITER+SYS.DBMS_LOGMNR.NO_ROWID_IN_STMT+SYS.DBMS_LOGMNR.DICT_FROM_ONLINE_CATALOG + SYS.DBMS_LOGMNR.STRING_LITERALS_IN_STMT); end;";
    public static final String MINING_END = "begin SYS.DBMS_LOGMNR.END_LOGMNR; end;";
    public static final String QUERY_REDO2 = "SELECT RN, SCN, COMMIT_SCN, XID, TIMESTAMP, COMMIT_TIMESTAMP, OPERATION, OPERATION_CODE, ROLLBACK, RS_ID, SEG_OWNER, TABLE_NAME, ROW_ID, CSF, SQL_REDO FROM (SELECT ROWNUM as RN, SCN, COMMIT_SCN, XID, TIMESTAMP, COMMIT_TIMESTAMP, OPERATION, OPERATION_CODE, ROLLBACK, RS_ID, SEG_OWNER, TABLE_NAME, ROW_ID, CSF, SQL_REDO FROM V$LOGMNR_CONTENTS WHERE OPERATION_CODE IN (7,36) OR OPERATION_CODE IN (1,2,3,5) AND (%s) AND ) WHERE RN > ? ";
    public static final String QUERY_REDO =  "SELECT RN, SCN, COMMIT_SCN, XID, TIMESTAMP, COMMIT_TIMESTAMP, OPERATION, OPERATION_CODE, ROLLBACK, RS_ID, SEG_OWNER, TABLE_NAME, ROW_ID, CSF, SQL_REDO FROM (SELECT ROWNUM as RN, SCN, COMMIT_SCN, XID, TIMESTAMP, COMMIT_TIMESTAMP, OPERATION, OPERATION_CODE, ROLLBACK, RS_ID, SEG_OWNER, TABLE_NAME, ROW_ID, CSF, SQL_REDO FROM V$LOGMNR_CONTENTS   ) WHERE RN > ? ";
//    public static final String QUERY_REDO =  "SELECT RN, SCN, COMMIT_SCN, XID, TIMESTAMP, COMMIT_TIMESTAMP, OPERATION, OPERATION_CODE, ROLLBACK, RS_ID, SEG_OWNER, TABLE_NAME, ROW_ID, CSF, SQL_REDO FROM (SELECT ROWNUM as RN, SCN, COMMIT_SCN, XID, TIMESTAMP, COMMIT_TIMESTAMP, OPERATION, OPERATION_CODE, ROLLBACK, RS_ID, SEG_OWNER, TABLE_NAME, ROW_ID, CSF, SQL_REDO FROM V$LOGMNR_CONTENTS WHERE OPERATION_CODE IN (1,2,3,5,7,36)) WHERE RN > ? ";
    public static final String QUERY_TALBE_DICT = "SELECT OWNER,TABLE_NAME,COLUMN_NAME,DATA_TYPE,DATA_LENGTH,DATA_PRECISION,NULLABLE,(SELECT 'Y' FROM DBA_CONS_COLUMNS PKCOL INNER JOIN DBA_CONSTRAINTS PK ON PK.CONSTRAINT_NAME = PKCOL.CONSTRAINT_NAME AND PK.OWNER = PKCOL.OWNER WHERE PK.CONSTRAINT_TYPE = 'P' AND PK.OWNER = COL.OWNER AND PK.TABLE_NAME = COL.TABLE_NAME AND PKCOL.COLUMN_NAME = COL.COLUMN_NAME) AS PK FROM DBA_TAB_COLUMNS COL WHERE COL.OWNER = ? AND COL.TABLE_NAME = ?";
    public static final String QUERY_TALBE_DICT_CDB = "select cn.OWNER, cn.TABLE_NAME, cn.COLUMN_NAME, cn.DATA_TYPE, cn.DATA_LENGTH, cn.DATA_PRECISION, cn.NULLABLE, cc.pk from (SELECT OWNER, TABLE_NAME, COLUMN_NAME, DATA_TYPE, DATA_LENGTH, DATA_PRECISION, NULLABLE FROM CDB_TAB_COLUMNS COL WHERE COL.OWNER = '$OWNER$' AND COL.TABLE_NAME = '$TABLE_NAME$') cn left outer join ( SELECT PKCOL.COLUMN_NAME,'Y' as pk FROM CDB_CONS_COLUMNS PKCOL INNER JOIN CDB_CONSTRAINTS PK  ON PK.CONSTRAINT_NAME = PKCOL.CONSTRAINT_NAME  AND PK.OWNER = PKCOL.OWNER WHERE PKCOL.CONSTRAINT_NAME = PK.CONSTRAINT_NAME AND PK.CONSTRAINT_TYPE = 'P' AND PK.OWNER = '$OWNER$' AND PK.TABLE_NAME = '$TABLE_NAME$') cc on cn.column_name = cc.column_name";

    public static final String QUEUE_REPLICATE = "mining:replicate";

    public static final String QUERY_SEQUENCE = "select SEQUENCE# from v$log where status='CURRENT' and THREAD#=?";
    public static final String QUERY_MAX_SEQUENCE_ARCHIVED = "select max(sequence#) from v$archived_log where THREAD#=?";
    public static final String QUERY_MIN_SEQUENCE_ARCHIVED = "select min(sequence#) from v$archived_log where  THREAD#=?";
    public static final String QUERY_MAX_SEQUENCE_ONLINE = "select max(sequence#) from v$log where THREAD#=?";

    public static final String QUERY_NAME_SEQUENCE_ARCHIVED = "select name from v$archived_log where sequence# = ? and THREAD#=?";
    public static final String QUERY_NAME_SEQUENCE_ONLINE = "select f.member as name from v$log l inner join v$logfile f on l.group# = f.group# where sequence#= ? and THREAD#=?";
    public static final String ADD_LOGFILE = "begin dbms_logmnr.add_logfile(?);end;";
}
