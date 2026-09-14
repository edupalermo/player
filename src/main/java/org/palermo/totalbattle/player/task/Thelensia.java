package org.palermo.totalbattle.player.task;

import lombok.extern.slf4j.Slf4j;
import org.palermo.totalbattle.player.TimeLeftUtil;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.selenium.leadership.Transformation;
import org.palermo.totalbattle.server.model.FlagInfo;
import org.palermo.totalbattle.server.model.FlagScenario;
import org.palermo.totalbattle.server.model.Player;
import org.palermo.totalbattle.util.FlagUtil;
import org.palermo.totalbattle.util.ImageUtil;
import org.palermo.totalbattle.util.Navigate;

import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
public class Thelensia {

    private final MyRobot robot = MyRobot.INSTANCE;
    private final Player player;

    public Thelensia(Player player) {
        this.player = player;
    }

    public void evaluate() {
        
        if (FlagUtil.isActive(player, FlagScenario.FREEZE_THELENSIA_CHEST_EVALUATION)) {
            FlagUtil.log(player, FlagScenario.FREEZE_THELENSIA_CHEST_EVALUATION);
            return;
        }

        Navigate navigate = Navigate.builder()
                .resourceName("player/thelensia/chest.png")
                .build();

        if (navigate.search().isEmpty()) {
            player.getFlags().put(FlagScenario.FREEZE_THELENSIA_CHEST_EVALUATION.name(), FlagInfo.builder()
                    .expiration(LocalDateTime.now().plusHours(20))
                    .createdAt(LocalDateTime.now())
                    .message("Player doesn't use Thelensia.")
                    .build());
            return;
        }
        
        Transformation transformation = Transformation.builder()
                .reference(Point.of(97, 196))
                .real(navigate.getPoint())
                .build();

        // ImageUtil.showImageAndWait(robot.captureScreen(transformation.transform(Point.of(68, 258), Point.of(168, 287))));
        
        BufferedImage slice = robot.captureScreen(transformation.transform(Point.of(68, 258), Point.of(168, 287)));
        BufferedImage timerIndicator = ImageUtil.loadResource("player/thelensia/timer_indicator.png");
        
        if (ImageUtil.search(timerIndicator, slice, 0.05).isEmpty()) {
            navigate.leftClick();
            log.info("Clicked on Thelensia chest");
            player.getFlags().put(FlagScenario.FREEZE_THELENSIA_CHEST_EVALUATION.name(), FlagInfo.builder()
                    .expiration(LocalDateTime.now().plusHours(20))
                    .createdAt(LocalDateTime.now())
                    .message("Waiting gifts to Thelensia.")
                    .build());

            return;
        }

        LocalDateTime nextLocalDateTime = TimeLeftUtil.parse(robot.captureScreen(transformation.transform(Point.of(83, 264), Point.of(148, 280))), new String[] {"FFF6C2"})
                .orElse(null);
        
        if (nextLocalDateTime != null && Duration.between(LocalDateTime.now(), nextLocalDateTime).abs().toHours() < 20) {
            player.getFlags().put(FlagScenario.FREEZE_THELENSIA_CHEST_EVALUATION.name(), FlagInfo.builder()
                    .expiration(nextLocalDateTime)
                    .createdAt(LocalDateTime.now())
                    .message("Waiting gifts to Thelensia.")
                    .build());
            return;
        }
    }
}
