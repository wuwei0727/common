package com.tgy.rtls.web.controller.sql;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.sqlbackup.SqlBackup;
import com.tgy.rtls.data.service.sqlbackup.SqlBackupService;
import com.tgy.rtls.web.util.DatabaseBack;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/sql")
@CrossOrigin
public class ExportSql {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Value("${spring.redis.host}")
    private String sqlip;
    private String port="3306";
    @Value("${spring.datasource.username}")
    private String user;
    @Value("${spring.datasource.password}")
    private String pwd;
    @Value("${file.url}")
    private String url;
    //上传真实地址
    @Value("${sql.uploadFolder}")
    private String uploadFolder;
    @Value("${audiofileurl}")
    private String http;
    @Autowired(required = false)
    private SqlBackupService sqlBackupService;

    /*每晚上12点更新一次*/
    @Scheduled(cron = "0 0 00 * * ? ")
    //@Scheduled(cron = "*/12 * * * * ?")
    public void dumpSql(){
        logger.info("备份数据库");
         startBackup();
         startBackup2();
         startBackup1();
    }


    @RequestMapping(value = "/exportSql")
    @ApiOperation(value = "导出数据库表内容",notes = "数据备份")
    public void addRoute(HttpServletResponse resp){

        try {
            long time=System.currentTimeMillis();
            String times=new Timestamp(time).toString();
            DatabaseBack.exportDatabaseTool(sqlip,port,user,pwd,"park1",uploadFolder+time+".sql");

            String downLoadFile = "http://"+http.split("/")[0] + "/" + url + "/" + time + ".sql";
            logger.error("sql down load address:"+downLoadFile);
            resp.sendRedirect(downLoadFile);
         //   resp.sendRedirect("http://112.94.22.123:10087/location.rar");
           // return "redirect:http://112.94.22.123:10087/location.rar";】
        } catch (Exception e) {
            e.printStackTrace();
            try {
                resp.sendRedirect("/page/home.html");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            //  return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }
    @RequestMapping(value = "/startBackup")
    @ApiOperation(value = "开始备份数据库文件---localpark",notes = "数据备份")
    public CommonResult<Object> startBackup(){

        try {
            long time=System.currentTimeMillis();
            LocalDate today = LocalDate.now();
            String times=new Timestamp(time).toString();
            logger.info("调用startBackup方法---->"+time);
            logger.info("调用startBackup方法---->"+times);
            //远程
//            DatabaseBack.exportDatabaseTool(sqlip,port,user,pwd,"park1",uploadFolder+time+".sql");
            //本地
            DatabaseBack.localExportDatabaseTool("127.0.0.1","3306","root","123456","park","E:/MysqlBackup/mysql/"+today.toString().replace("-", "")+".sql");
            String downLoadFile = "http://"+http.split("/")[0] + url + today + ".sql";
            logger.error("sql down load address:"+downLoadFile);
            SqlBackup sqlBackup=new SqlBackup();
            sqlBackup.setLocalPath(uploadFolder+today+".sql");
            sqlBackup.setDownloadPath( url  + today + ".sql");
            sqlBackup.setTime(new Date());
            sqlBackupService.addSqlBackupFile(sqlBackup);
           return new CommonResult<>(200,"备份成功");

        } catch (Exception e) {
            e.printStackTrace();
          /*  try {
             // resp.sendRedirect("/page/home.html");
            } catch (Exception ex) {
                ex.printStackTrace();
            }*/
            //
        }
        return new CommonResult<>(500, "备份失败");
    }

    @RequestMapping(value = "/startBackup2")
    @ApiOperation(value = "开始备份数据库文件----95park1",notes = "数据备份")
    public CommonResult<Object> startBackup2(){

        try {
            long time=System.currentTimeMillis();
            String times=new Timestamp(time).toString();
            logger.info("调用startBackup方法---->"+time);
            logger.info("调用startBackup方法---->"+times);
            //远程
            DatabaseBack.localExportDatabaseTool("192.168.1.95","3306","root","tuguiyao","park1","E:/MysqlBackup/park1/"+time+".sql");
            String downLoadFile = "http://"+http.split("/")[0] + url + time + ".sql";
            logger.error("sql down load address:"+downLoadFile);
            SqlBackup sqlBackup=new SqlBackup();
            sqlBackup.setLocalPath(uploadFolder+time+".sql");
            sqlBackup.setDownloadPath( url  + time + ".sql");
            sqlBackup.setTime(new Date());
            sqlBackupService.addSqlBackupFile(sqlBackup);
            return new CommonResult<>(200,"备份成功");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new CommonResult<>(500, "备份失败");
    }

    @RequestMapping(value = "/startBackup1")
    @ApiOperation(value = "开始备份数据库文件----95park1",notes = "数据备份")
    public CommonResult<Object> startBackup1(){

        try {
            long time=System.currentTimeMillis();
            String times=new Timestamp(time).toString();
            logger.info("调用startBackup方法---->"+time);
            logger.info("调用startBackup方法---->"+times);
            //远程
            DatabaseBack.exportDatabaseTool("192.168.1.95","3306","root","tuguiyao","park1",uploadFolder+time+".sql");
            String downLoadFile = "http://"+http.split("/")[0] + url + time + ".sql";
            logger.error("sql down load address:"+downLoadFile);
            SqlBackup sqlBackup=new SqlBackup();
            sqlBackup.setLocalPath(uploadFolder+time+".sql");
            sqlBackup.setDownloadPath( url  + time + ".sql");
            sqlBackup.setTime(new Date());
            sqlBackupService.addSqlBackupFile(sqlBackup);
            return new CommonResult<>(200,"备份成功");

        } catch (Exception e) {
            e.printStackTrace();
          /*  try {
             // resp.sendRedirect("/page/home.html");
            } catch (Exception ex) {
                ex.printStackTrace();
            }*/
            //
        }
        return new CommonResult<>(500, "备份失败");
    }

    @RequestMapping(value = "/getBackupSql")
    @ApiOperation(value = "查询备份文件",notes = "数据备份")
    public  CommonResult getSqlBackupFile(String startTime, String endTime, Integer flag, Integer pageIndex, Integer pageSize) {

        try {
            CommonResult<Object> res = new CommonResult<>();
            if(pageSize!=-1) {
                PageHelper.startPage(pageIndex, pageSize);
            }
            List<SqlBackup> list = sqlBackupService.getSqlBackFileList(startTime, endTime,flag);
            PageInfo<SqlBackup> pageInfo=new PageInfo<>(list);
            Map<String,Object> result=new HashMap<>();
            result.put("list",pageInfo.getList());
            result.put("pageIndex", pageIndex);
            result.put("total", pageInfo.getTotal());
            result.put("pages", pageInfo.getPages());
            if(pageSize!=null&&pageSize!=-1) {
                res.setData(result);
            }
            res.setData(list);
            res.setCode(200);
            res.setMessage("查询成功");
            return res;

        } catch (Exception e) {
            e.printStackTrace();

        }
        return new CommonResult<>(500, "查询失败");
    }

    @RequestMapping(value = "/startRecovery")
    @ApiOperation(value = "开始还原备份数据",notes = "还原")
    public  CommonResult getSqlBackupFile(Integer id) {

        try {
            SqlBackup sqlBackup = sqlBackupService.findSqlBackFileById(id);
            List<SqlBackup> s112 = sqlBackupService.getSqlBackFileList(null, null, null);
            logger.error("size:"+s112.size());
            logger.error("backup stasrt"+ new Timestamp(new Date().getTime()).toString());
            DatabaseBack.importDatabase(sqlip,port,user,pwd,sqlBackup.getLocalPath(),null,"rtls");
            List<SqlBackup> list = sqlBackupService.getSqlBackFileList(null, null, 1);
            if(list!=null){
                for (SqlBackup sql:list
                     ) {
                    sqlBackupService.updateSqlFlag(sql.getId(),0);
                }
            }
          //  logger.error("recoveryuend"+ new Timestamp(new Date().getTime()).toString());
            List<SqlBackup> s11 = sqlBackupService.getSqlBackFileList(null, null, null);
          //  logger.error("size:"+new Timestamp(sqlBackup.getTime().getTime()).toString());
            sqlBackupService.addSqlBackupFile(sqlBackup);
           // logger.error("sqlBackup.getId()"+sqlBackup.getId());
            sqlBackupService.updateSqlFlag(sqlBackup.getId(),1);
            CommonResult<Object> res = new CommonResult<>();
            res.setData(sqlBackup);
            res.setCode(200);
            res.setMessage("还原成功");
            return res;
        } catch (Exception e) {
           logger.error( e.getMessage());
        }
        return new CommonResult<>(500, "还原失败");
    }
}
