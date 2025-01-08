package com.tgy.rtls.web.util;

import com.tgy.rtls.data.tool.ExecLinuxCMD;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ResourceUtils;

import java.io.File;
@Slf4j
public class DatabaseBack {
    /**

     * Java代码实现MySQL数据库导出

     *

     * @param hostIP      MySQL数据库所在服务器地址IP

     * @param userName    进入数据库所需要的用户名

     * @param password    进入数据库所需要的密码



     * @param databaseName 要导出的数据库名

     * @return 返回true表示导出成功，否则返回false。

     * @author GaoHuanjie

     */

    public static void exportDatabaseTool(String hostIP, String hostPort, String userName, String password, String databaseName,String targetPath) throws Exception {

      /*  File saveFile =new File(savePath);

        if (!saveFile.exists()) {// 如果目录不存在
           saveFile.mkdirs();// 创建文件夹
 }

        if (!savePath.endsWith(File.separator)) {

            savePath = savePath + File.separator;

        }*/

   /*     PrintWriter printWriter =null;

        BufferedReader bufferedReader =null;
*/
     /*   try {*/

            Runtime runtime = Runtime.getRuntime();

            String path = ResourceUtils.getURL("classpath:static").getPath().replace('/','\\');

            path = path.substring(1);

         //   log.info(path);

//String cmd = "mysqldump -h127.0.0.1 -uroot -P3308 -p123456 archives";

           String cmd ="/data/docker/dockerdata/tomcatWork/mysqldump -h" + hostIP +" -u" + userName +" -P" + hostPort +" -p" + password +" " + databaseName+" >"+targetPath;
           log.info("调用exportDatabaseTool方法--cmd----"+cmd);

          //  cmd = path +"\\" + cmd;

           // log.info(cmd);

          //  Process process = runtime.exec(cmd);
            ExecLinuxCMD.exec(cmd);

          //  InputStreamReader inputStreamReader =new InputStreamReader(process.getInputStream(),"utf8");

           // bufferedReader =new BufferedReader(inputStreamReader);

            //printWriter =new PrintWriter(new OutputStreamWriter(new FileOutputStream(savePath + fileName),"utf8"));

          /*  String line;

            while ((line = bufferedReader.readLine()) !=null) {

                //printWriter.println(line);
                out.write(line.getBytes());

            }

           // printWriter.flush();
            out.flush();

            if (process.waitFor() ==0) {//0 表示线程正常终止。

                 return true;

            }*/

      /*  }catch (IOException e) {

            e.printStackTrace();

        }finally {*/

          /*  try {

                if (bufferedReader !=null) {

                    bufferedReader.close();

                }
               // out.close();

                if (printWriter !=null) {

                    printWriter.close();

                }

            }catch (IOException e) {

                e.printStackTrace();

            }
*/
    /*    }*/



    }
    public static void localExportDatabaseTool(String hostIP, String hostPort, String userName, String password, String databaseName,String targetPath) throws Exception {
        Runtime runtime = Runtime.getRuntime();
        String path = ResourceUtils.getURL("classpath:static").getPath().replace('/','\\');
        path = path.substring(1);
        String cmd ="mysqldump -h" + hostIP +" -u" + userName +" -P" + hostPort +" -p" + password +" " + databaseName+" >"+targetPath;
        log.info("cmd----"+cmd);
        ExecLinuxCMD.localExec(cmd);


    }
    /**

     * Java实现MySQL数据库导入

     *

     * @param hostIP        MySQL数据库所在服务器地址IP

     * @param userName      数据库用户名

     * @param password      进入数据库所需要的密码

     * @param importFilePath 数据库文件路径

     * @param sqlFileName    数据库文件名

     * @param databaseName  要导入的数据库名

     * @return 返回true表示导入成功，否则返回false。

     * @author GaoHuanjie

     */

    public static boolean importDatabase(String hostIP, String hostPort, String userName, String password, String importFilePath, String sqlFileName, String databaseName) throws Exception {

        File saveFile =new File(importFilePath);

        if (!saveFile.exists()) {// 如果目录不存在

            saveFile.mkdirs();// 创建文件夹
            }

   /*     if (!importFilePath.endsWith(File.separator)) {

            importFilePath = importFilePath + File.separator;

        }*/

        StringBuilder stringBuilder =new StringBuilder();

        stringBuilder.append("mysql").append(" -h").append(hostIP);

        stringBuilder.append(" -u").append(userName).append(" -P").append(hostPort).append(" -p").append(password);

        stringBuilder.append(" ").append(databaseName);

        //stringBuilder.append(" <").append(importFilePath).append(sqlFileName);
        stringBuilder.append(" <").append(importFilePath);

       // try {

        /*    Process process = Runtime.getRuntime().exec("cmd /c " + stringBuilder.toString());//必须要有“cmd /c ”

                if (process.waitFor() ==0) {// 0 表示线程正常终止。
                     return true;
            }*/
        String dasd=stringBuilder.toString();
            System.out.println(dasd);
            ExecLinuxCMD.exec(stringBuilder.toString());

      /*  }catch (Exception e) {

            e.printStackTrace();

        }*/

        return false;

    }

    public static void main(String[] args) {
        try {
            importDatabase("192.168.1.95","3306","root","tuguiyao","/data/docker/dockeryml/","11.sql","rtls");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }








}
