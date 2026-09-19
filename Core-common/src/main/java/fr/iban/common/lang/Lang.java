package fr.iban.common.lang;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.Map;

public class Lang {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final String locale;
    private final Map<String, String> entries;

    public Lang(String locale, Map<String, String> entries) {
        this.locale = locale;
        this.entries = entries;
    }

    public String getLocale() {
        return locale;
    }

    public boolean contains(String key) {
        return entries.containsKey(key);
    }

    public String getRaw(String key) {
        return entries.get(key);
    }

    public Component get(String key, TagResolver... resolvers) {
        String raw = entries.get(key);
        if (raw == null) {
            return Component.text("<missing:" + key + ">");
        }
        return MINI_MESSAGE.deserialize(raw, resolvers);
    }
}
