package org.palermo.totalbattle.util;

import lombok.extern.slf4j.Slf4j;
import org.palermo.totalbattle.server.model.FlagInfo;
import org.palermo.totalbattle.server.model.FlagScenario;
import org.palermo.totalbattle.server.model.Player;
import org.palermo.totalbattle.util.bean.ConfigurationMode;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
public class FlagUtil {
    
    public static boolean isActive(Player player, FlagScenario scenario) {
        FlagInfo flagInfo = player.getFlags().get(scenario.name());
        return flagInfo != null && flagInfo.getExpiration().isAfter(LocalDateTime.now());
    }

    public static String duration(FlagInfo flagInfo) {
        Duration duration = Duration.between(LocalDateTime.now(), flagInfo.getExpiration());

        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        StringBuilder result = new StringBuilder();

        if (days > 0) {
            result.append(days).append(" d ");
        }

        if (hours > 0 || days > 0) {
            result.append(hours).append(" h ");
        }

        if (minutes > 0 || hours > 0 || days > 0) {
            result.append(minutes).append(" m ");
        }

        result.append(seconds).append(" s");

        return result.toString();
    }
    
    public static void log(Player player, FlagScenario scenario) {
        FlagInfo flagInfo = player.getFlags().get(scenario.name());
        log.info(String.format("Flag %s [%s]: %s ", scenario.name(), FlagUtil.duration(flagInfo), flagInfo.getMessage()));
    }
}
