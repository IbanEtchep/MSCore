package fr.iban.bukkitcore.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class ChatUtils {
    
    private ChatUtils() {}

    public static Component translateColors(String string) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(string);
    }

    public static String toPlainText(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }
}
