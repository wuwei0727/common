package com.tgy.rtls.data.mapper.sqlbackup;

import com.tgy.rtls.data.entity.sqlbackup.SqlBackup;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.mapper.location
 * @date 2020/10/22
 */
public interface SqlBackupMapper {

   List<SqlBackup> getSqlBackFileList(@Param("start") String start,@Param("end") String end,@Param("flag")Integer flag);
   void addSqlBackupFile(@Param("sqlBackup")SqlBackup sqlBackup);
   SqlBackup findSqlBackFileById(@Param("id")Integer id);
   void updateSqlFlag(@Param("id")Integer id,@Param("flag")Integer flag);
}
