package fr.iban.survivalcore.lang;

import fr.iban.survivalcore.utils.PlaceholderProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageBuilder {

    private final String msg;
    private final Map<String, String> placeholders = new HashMap<>();
    private final List<PlaceholderProvider> providers = new ArrayList<>();

    private MessageBuilder(String msg) {
        this.msg = msg;
    }

    public static MessageBuilder translatable(String key) {
        return new MessageBuilder(SurvivalTranslator.get().raw(key));
    }

    public static MessageBuilder translatable(LangKey key) {
        return new MessageBuilder(SurvivalTranslator.get().raw(key.getTranslationKey()));
    }

    public MessageBuilder placeholder(String key, String value) {
        placeholders.put(key, value);
        return this;
    }

    public MessageBuilder applyPlaceholder(PlaceholderProvider provider) {
        providers.add(provider);
        return this;
    }

    public String toRaw() {
        String out = msg;

        for (var entry : placeholders.entrySet())
            out = out.replace("%" + entry.getKey() + "%", entry.getValue());

        for (PlaceholderProvider p : providers)
            out = p.apply(out);

        return out;
    }

    public Component toComponent() {
        if (msg.contains("<") && msg.contains(">"))
            return MiniMessage.miniMessage().deserialize(toRaw());

        return LegacyComponentSerializer.legacyAmpersand().deserialize(toRaw());
    }

    public String toLegacy() {
        return LegacyComponentSerializer.legacySection().serialize(toComponent());
    }

    public List<String> toLegacyList() {
        return Arrays.asList(toLegacy().split("\n"));
    }

    public Component toLegacyComponent() {
        return LegacyComponentSerializer.legacySection().deserialize(toLegacy());
    }

    public List<Component> toLegacyComponents() {
        List<Component> list = new ArrayList<>();
        for (String line : toLegacyList()) {
            list.add(LegacyComponentSerializer.legacySection().deserialize(line));
        }
        return list;
    }
}
