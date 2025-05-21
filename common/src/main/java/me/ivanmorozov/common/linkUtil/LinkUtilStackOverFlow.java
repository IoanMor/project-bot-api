package me.ivanmorozov.common.linkUtil;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkUtilStackOverFlow {
    public static boolean validationLink(String link) {
        return link != null && link.matches("https://stackoverflow\\.com/questions/\\d+.*");
    }
    public static String normalizeLink(String link) {
        return link.replaceAll("(?i)/questions/(\\d+).*", "/questions/$1");
    }
    public static Optional<Long> parseQuestionId(String link) {
        try {
            Pattern pattern = Pattern.compile("stackoverflow\\.com/questions/(\\d+)");
            Matcher matcher = pattern.matcher(link);
            if (matcher.find()) {
                return Optional.of(Long.parseLong(matcher.group(1)));
            }
            return Optional.empty();
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
