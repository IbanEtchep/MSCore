package fr.iban.survivalcore.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.md_5.bungee.api.ChatColor;

public enum HexColor {

    OLIVE("#708d23"),
    MARRON("#B95608"),
    MARRON_CLAIR("#fbeba7"),
    ROUGE_ORANGE("#ee2400"),
    FLAT_BLUE("#0984e3"),
    FLAT_BLUE_GREEN("#00cec9"),
    FLAT_PURPLE("#6c5ce7"),
    FLAT_LIGHT_RED("#ff7675"),
    FLAT_RED("#d63031"),
    FLAT_PINK("#e84393"),
    FLAT_GREEN("#00b894"),
    FLAT_LIGHT_GREEN("#55efc4"),
    FLAT_BLUE2("#3742fa");

    private String hex;
    public static final char COLOR_CHAR = '\u00A7';

    private HexColor(String hex) {
        this.hex = hex;
    }


    public String getHex() {
        return hex;
    }

    public ChatColor getColor() {
        return ChatColor.of(hex);
    }


    public static String translateColorCodes(String startTag, String endTag, String message)
    {
        final Pattern hexPattern = Pattern.compile(startTag + "([A-Fa-f0-9]{6})" + endTag);
        Matcher matcher = hexPattern.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
        while (matcher.find())
        {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, COLOR_CHAR + "x"
                    + COLOR_CHAR + group.charAt(0) + COLOR_CHAR + group.charAt(1)
                    + COLOR_CHAR + group.charAt(2) + COLOR_CHAR + group.charAt(3)
                    + COLOR_CHAR + group.charAt(4) + COLOR_CHAR + group.charAt(5)
            );
        }
        return ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());
    }

    public static String translateColorCodes(String message)
    {
        return translateColorCodes("#", "", message);
    }

    public static List<String> translateColorCodes(List<String> message)
    {
        List<String> translated = new ArrayList<>();
        for(String msg : message) {
            translated.add(translateColorCodes(msg));
        }
        return translated;
    }


}
