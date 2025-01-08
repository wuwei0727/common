package com.tgy.rtls.data.service.sqlbackup.impl;


import com.tgy.rtls.data.entity.sqlbackup.SqlBackup;
import com.tgy.rtls.data.mapper.sqlbackup.SqlBackupMapper;
import com.tgy.rtls.data.service.sqlbackup.SqlBackupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.routing.impl
 * @date 2020/11/24
 */
@Service
@Transactional
public class SqlBackupServiceImpl implements SqlBackupService {
    @Autowired(required = false)
    private SqlBackupMapper sqlBackupMapper;


    @Override
    public List<SqlBackup> getSqlBackFileList(String start, String end,Integer flag) {
        return sqlBackupMapper.getSqlBackFileList(start,end,flag);
    }

    @Override
    public SqlBackup findSqlBackFileById(Integer id) {
        return sqlBackupMapper.findSqlBackFileById(id);
    }

    @Override
    public void addSqlBackupFile(SqlBackup sqlBackup) {
            sqlBackupMapper.addSqlBackupFile(sqlBackup);
    }

    @Override
    public void updateSqlFlag(Integer id, Integer flag) {
        sqlBackupMapper.updateSqlFlag(id,flag);
    }
}
