package org.palermo.totalbattle;

import org.palermo.totalbattle.selenium.leadership.MyRobot;

import java.util.regex.Pattern;

public class AutomatizeDailyJobs {

    private static final MyRobot robot = MyRobot.INSTANCE;

    private static final Pattern PERCENT = Pattern.compile("^[0-9]{1,2}%$");

    public static void main(String arg[]) {
        execute(49); // 8 is max
    }

    private static void execute(int times) {
        for (int c = 0; c < times; c++) {
            System.out.println(String.format("%d / %d", c + 1, times));
            // Click on Add
            robot.fastLeftClick(1289, 868);
            robot.sleep(100);

            // Click on Use
            robot.fastLeftClick(1157, 551);
            robot.sleep(100);

            // Make popup disappear
            robot.fastLeftClick(1287, 503);
            robot.sleep(100);

            // Click of free button
            for (int i = 0; i < 9; i++) {
                robot.fastLeftClick(1287, 503);
            }
        }
    }
}
