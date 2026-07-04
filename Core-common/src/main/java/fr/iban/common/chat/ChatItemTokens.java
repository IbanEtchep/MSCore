package fr.iban.common.chat;

import java.util.regex.Pattern;

public final class ChatItemTokens {

    private static final Pattern TOKEN = Pattern.compile("\\[(?:i|item)\\]");

    private ChatItemTokens() {
    }

    public static boolean containsToken(String message) {
        return message != null && TOKEN.matcher(message).find();
    }

    public static String replaceWithSentinel(String message) {
        if (message == null) {
            return null;
        }
        return TOKEN.matcher(message).replaceAll(ChatItemConstants.ITEM_PLACEHOLDER);
    }
}
