package com.tgy.rtls.data.tool;

/**
 * java在linux环境下执行linux命令，然后返回命令返回值。
 * @author lee
 */

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

public class ExecLinuxCMD {

    public static Object exec(String cmd) throws IOException {
      /*  try {*/
            String[] cmdA = { "/bin/sh", "-c", cmd };
            //String[] cmdA = { "cmd", "/c", cmd };
            Process process = Runtime.getRuntime().exec(cmdA);
            LineNumberReader br = new LineNumberReader(new InputStreamReader(
                    process.getInputStream()));
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                sb.append(line).append("\n");
            }

            return sb.toString();
       /* } catch (Exception e) {
            e.printStackTrace();
        }*/
       // return null;
    }

    public static Object localExec(String cmd) throws IOException {
        String[] cmdA = { "cmd", "/c", cmd };
        Process process = Runtime.getRuntime().exec(cmdA);
        LineNumberReader br = new LineNumberReader(new InputStreamReader(
                process.getInputStream()));
        StringBuffer sb = new StringBuffer();
        String line;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
            sb.append(line).append("\n");
        }

        return sb.toString();
    }
    public static void main(String[] args) {
        // TODO Auto-generated method stub
       // String pwdString = exec("pwd").toString();
       // String netsString = exec("netstat -nat|grep -i \"80\"|wc -l").toString();

      //  System.out.println("==========获得值=============");
        //System.out.println(pwdString);
        //System.out.println(netsString);
    }

}
