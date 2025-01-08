package com.tgy.rtls.data.service.sqlbackup;

import com.tgy.rtls.data.entity.sqlbackup.SqlBackup;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.routing
 * @date 2020/11/23
 */
public interface SqlBackupService {
    List<SqlBackup> getSqlBackFileList( String start,  String end,Integer flag);
     SqlBackup findSqlBackFileById(Integer id);
    void addSqlBackupFile(SqlBackup sqlBackup);
   void updateSqlFlag(Integer id,Integer flag);

}
