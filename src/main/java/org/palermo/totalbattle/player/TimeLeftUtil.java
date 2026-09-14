package org.palermo.totalbattle.player;

import org.palermo.totalbattle.util.ImageUtil;
import org.palermo.totalbattle.util.OcrUtil;

import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeLeftUtil {

    public static Optional<LocalDateTime> parse(BufferedImage input, String[] mainColor) {
        BufferedImage timeLeft = ImageUtil.toGrayscale(input, mainColor);
        timeLeft = ImageUtil.linearNormalization(timeLeft);
        timeLeft = ImageUtil.cropText(timeLeft);
        timeLeft = ImageUtil.linearNormalization(timeLeft);
        if (timeLeft.getHeight() < 100) {
            timeLeft = ImageUtil.resize(timeLeft, 100);
        }
        // ImageUtil.showImageAndWait(timeLeft);
        String timeLeftAsString = OcrUtil.ocr(timeLeft, OcrUtil.WHITELIST_FOR_COUNTDOWN, OcrUtil.PATTERN_FOR_COUNTDOWN);
        return parse(timeLeftAsString);
    }

    public static Optional<LocalDateTime> parse(String input) {
        Pattern pattern = Pattern.compile("(\\d+)h[:]?([\\d+]+)m");
        Matcher matcher = pattern.matcher(input.trim());

        int days = 0;
        int hours = 0;
        int minutes = 0;
        int seconds = 0;
        
        int error = 0;

        boolean parsed = false;

        if (matcher.matches()) {
            hours = Integer.parseInt(matcher.group(1));
            minutes = Integer.parseInt(matcher.group(2));
            error = 59;
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
                error = (59 * 60) + 59;
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
                .plusSeconds(seconds)
                .plusSeconds(error);

        return Optional.of(answer);
    }
}
