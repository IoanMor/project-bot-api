package me.ivanmorozov.common.linkUtil;

public class LinkUtilStackOverFlow {
    public static boolean validationLink(String link) {
        return link != null && link.matches("https://stackoverflow\\.com/questions/\\d+.*");
    }
    public static String normalizeLink(String link) {
        return link.replaceAll("(?i)/questions/(\\d+).*", "/questions/$1");
    }
}
