package fr.iban.velocitycore.lang;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.HashMap;
import java.util.Map;

public class MessageBuilder {

    private final String raw;
    private final Map<String, String> placeholders = new HashMap<>();

    private MessageBuilder(String raw) {
        this.raw = raw;
    }

    public static MessageBuilder translatable(LangKey key) {
        return new MessageBuilder(VelocityTranslator.get().raw(key.getTranslationKey()));
    }

    public static MessageBuilder translatable(String key) {
        return new MessageBuilder(VelocityTranslator.get().raw(key));
    }

    public MessageBuilder placeholder(String key, String value) {
        placeholders.put(key, value);
        return this;
    }

    public String toStringRaw() {
        String out = raw;
        for (var e : placeholders.entrySet()) {
            out = out.replace("%" + e.getKey() + "%", e.getValue());
        }
        return out;
    }

    public Component toComponent() {
        return MiniMessage.miniMessage().deserialize(toStringRaw());
    }

    public String toLegacy() {
        return LegacyComponentSerializer.legacySection().serialize(toComponent());
    }
}
