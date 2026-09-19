package fr.iban.common.lang;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public abstract class LangManager {

    protected Lang active;
    protected Lang fallback;

    public Lang getActive() {
        return active;
    }

    public Lang getFallback() {
        return fallback;
    }

    public Component get(String key, TagResolver... resolvers) {
        if (active != null && active.contains(key)) {
            return active.get(key, resolvers);
        }
        if (fallback != null && fallback.contains(key)) {
            return fallback.get(key, resolvers);
        }
        return Component.text("<missing:" + key + ">");
    }

    public abstract void reload();
}
