package com.tgy.rtls.web.util;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.web.util
 * @Author: wuwei
 * @CreateTime: 2023-07-05 17:12
 * @Description: TODO
 * @Version: 1.0
 */
public class QqSendMsg01 {
        public static void main(String[] args) {
            try {
                Robot robot = new Robot();
                robot.delay(5000);
                for (int i = 0; i < 1; i++) {
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection("你好，帅小伙，学Java吗？"), null);
                    robot.keyPress(KeyEvent.VK_CONTROL);
                    robot.keyPress(KeyEvent.VK_V);
                    robot.keyRelease(KeyEvent.VK_CONTROL);
                    robot.keyRelease(KeyEvent.VK_V);
                    robot.keyPress(KeyEvent.VK_ENTER);
                    robot.keyRelease(KeyEvent.VK_ENTER);
                    robot.delay(5000);
                }
                //无限炸，这个快
/*            while (true) {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection("你好，帅小伙"), null);
                robot.keyPress(KeyEvent.VK_CONTROL);
                robot.keyPress(KeyEvent.VK_V);
                robot.keyRelease(KeyEvent.VK_CONTROL);
                robot.keyRelease(KeyEvent.VK_V);
                robot.keyPress(KeyEvent.VK_ENTER);
                robot.keyRelease(KeyEvent.VK_ENTER);
                robot.delay(100);
            }*/
            } catch (AWTException e) {
                e.printStackTrace();
            }
        }
}
