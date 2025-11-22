package org.palermo.totalbattle.player;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeLeftUtil {

    public static Optional<LocalDateTime> parse(String input) {
        Pattern pattern = Pattern.compile("(\\d+)h[:]?([\\d+]+)m");
        Matcher matcher = pattern.matcher(input.trim());

        int days = 0;
        int hours = 0;
        int minutes = 0;
        int seconds = 0;

        boolean parsed = false;

        if (matcher.matches()) {
            hours = Integer.parseInt(matcher.group(1));
            minutes = Integer.parseInt(matcher.group(2));
            parsed = true;
        }

        if (!parsed) {
            pattern = Pattern.compile("(\\d+)m[:]?([\\d+]+)5");
            matcher = pattern.matcher(input.trim());
            if (matcher.matches()) {
                minutes = Integer.parseInt(matcher.group(1));
                seconds = Integer.parseInt(matcher.group(2));
                parsed = true;
            }
        }

        if (!parsed) {
            pattern = Pattern.compile("(\\d+)m[:]?([\\d+]+)s");
            matcher = pattern.matcher(input.trim());
            if (matcher.matches()) {
                minutes = Integer.parseInt(matcher.group(1));
                seconds = Integer.parseInt(matcher.group(2));
                parsed = true;
            }
        }

        if (!parsed) {
            pattern = Pattern.compile("^(\\d+)s$");
            matcher = pattern.matcher(input.trim());
            if (matcher.matches()) {
                seconds = Integer.parseInt(matcher.group(1));
                parsed = true;
            }
        }

        if (!parsed) {
            pattern = Pattern.compile("(\\d+)d[:]?([\\d+]+)h");
            matcher = pattern.matcher(input.trim());
            if (matcher.matches()) {
                days = Integer.parseInt(matcher.group(1));
                hours = Integer.parseInt(matcher.group(2));
                parsed = true;
            }
        }

        if (!parsed) {
            throw new RuntimeException("Impossible to parse " + input);
        }

        LocalDateTime answer = LocalDateTime.now()
                .plusDays(days)
                .plusHours(hours)
                .plusMinutes(minutes)
                .plusSeconds(seconds);

        return Optional.of(answer);
    }
}
