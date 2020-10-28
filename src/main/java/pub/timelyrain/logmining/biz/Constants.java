package pub.timelyrain.logmining.biz;

public class Constants {
    public static final String MINING_START = "begin SYS.DBMS_LOGMNR.START_LOGMNR(STARTSCN => ?, ENDSCN => ? ,OPTIONS =>  SYS.DBMS_LOGMNR.SKIP_CORRUPTION+SYS.DBMS_LOGMNR.NO_SQL_DELIMITER+SYS.DBMS_LOGMNR.NO_ROWID_IN_STMT+SYS.DBMS_LOGMNR.DICT_FROM_ONLINE_CATALOG + SYS.DBMS_LOGMNR.CONTINUOUS_MINE+SYS.DBMS_LOGMNR.COMMITTED_DATA_ONLY+SYS.DBMS_LOGMNR.STRING_LITERALS_IN_STMT);  end;";
    public static final String MINING_END = "begin SYS.DBMS_LOGMNR.END_LOGMNR; end;";
    public static final String QUERY_REDO = "SELECT thread#, scn, start_scn, nvl(commit_scn,scn) commit_scn ,(xidusn||'.'||xidslt||'.'||xidsqn) AS xid,timestamp, operation_code, operation,status, SEG_TYPE_NAME ,info,seg_owner, table_name, username, sql_redo ,row_id, csf, TABLE_SPACE, SESSION_INFO, RS_ID, RBASQN, RBABLK, SEQUENCE#, TX_NAME, SEG_NAME, SEG_TYPE_NAME FROM  v$logmnr_contents  WHERE OPERATION_CODE in (1,2,3,5) ";
    public static final String QUERY_CURRENT_SCN = "select current_scn from v$database";
    public static final String QUEYR_TALBE_DICT = "SELECT OWNER,TABLE_NAME,COLUMN_NAME,DATA_TYPE,DATA_LENGTH,DATA_PRECISION,NULLABLE,(SELECT 'Y' FROM DBA_CONS_COLUMNS PKCOL INNER JOIN DBA_CONSTRAINTS PK ON PK.CONSTRAINT_NAME = PKCOL.CONSTRAINT_NAME AND PK.OWNER = PKCOL.OWNER WHERE PK.CONSTRAINT_TYPE = 'P' AND PK.OWNER = COL.OWNER AND PK.TABLE_NAME = COL.TABLE_NAME AND PKCOL.COLUMN_NAME = COL.COLUMN_NAME) AS PK FROM DBA_TAB_COLUMNS COL WHERE COL.OWNER = ? AND COL.TABLE_NAME = ?";

}