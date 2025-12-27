package org.palermo.totalbattle.player.task;

import lombok.extern.slf4j.Slf4j;
import org.palermo.totalbattle.player.Player;
import org.palermo.totalbattle.selenium.leadership.Area;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.selenium.leadership.Transformation;
import org.palermo.totalbattle.util.Navigate;

import java.awt.event.KeyEvent;

@Slf4j
public class FixBrokenArmor {

    private Player player;
    private final MyRobot robot = MyRobot.INSTANCE;

    public FixBrokenArmor(Player player) {
        this.player = player;
    }
    
    public void fix() {

        Navigate navigate = Navigate.builder()
                .areaName("MAIN_HERO_PICTURE")
                .resourceName("player/hero/broken_armor_66.png")
                .comparationLimit(0.05)
                .build();
        
        if (!navigate.exist()) {
            log.info("Armor is NOT broken! Good to go!");
            return;
        }
        
        navigate.leftClick();     

        navigate = Navigate.builder()
                .areaName(Area.MANAGE_CAPTAINS_CLOSE_BUTTON)
                .resourceName("player/hero/button_close.png")
                .waitLimit(7500)
                .build().ensureExistence();
        
        Transformation transformation = Transformation.builder()
                .real(navigate.getPoint())
                .reference(Point.of(1453, 338))
                .build();

        navigate = Navigate.builder()
                .area(transformation.transform(Point.of(581, 842), Point.of(975, 924)))
                .resourceName("player/hero/broken_armor_66.png")
                .comparationLimit(0.05)
                .build();
        
        robot.mouseMove(Point.of(100, 100));
        
        int count = 0;
        
        while(navigate.searchAgain().isPresent()) { // LIMIT ?
            if (count >= 4) {
                throw new RuntimeException("It shouldn't run forever!");
            }

            robot.leftClick(navigate.getPoint().move(14, 0));
            
            handleArmor(transformation, transformation.transform(Point.of(958, 393), Point.of(1039, 674)));
            handleArmor(transformation, transformation.transform(Point.of(529, 393), Point.of(669, 674)));
            
            count = count + 1;
        }

        robot.type(KeyEvent.VK_ESCAPE);
        robot.sleep(300);
    }
    
    private void handleArmor(Transformation transformation, Area area) {
        Navigate navigate = Navigate.builder()
                .area(area)
                .resourceName("player/hero/broken_armor_66.png")
                .comparationLimit(0.05)
                .build();

        robot.mouseMove(Point.of(100, 100));

        int count = 0;
        while(navigate.searchAgain().isPresent()) {
            if (count >= 3) {
                throw new RuntimeException("It shouldn't run forever!");
            }

            robot.leftClick(navigate.getPoint().move(14, 0));

            robot.sleep(5000); // Remove me!
            
            Navigate navigateTitle = Navigate.builder()
                    .areaName(Area.MANAGE_CAPTAINS_EQUIPMENT_TITLE)
                    .resourceName("player/hero/title_equipment.png")
                    .waitLimit(5000)
                    .build()
                    .ensureExistence();
            
            robot.leftClick(navigateTitle.getPoint().move(-110, 247));
            robot.sleep(1000);

            robot.type(KeyEvent.VK_ESCAPE);
            robot.sleep(300);

            count = count + 1;
        }
    }
}
