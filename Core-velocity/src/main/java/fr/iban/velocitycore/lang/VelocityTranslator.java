package fr.iban.velocitycore.lang;

import com.velocitypowered.api.proxy.ProxyServer;
import fr.iban.velocitycore.CoreVelocityPlugin;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class VelocityTranslator {

    private static VelocityTranslator INSTANCE;

    private final CoreVelocityPlugin plugin;
    private final Logger logger;

    private String currentLanguage;
    private Map<String, Object> yaml = new HashMap<>();

    public VelocityTranslator(CoreVelocityPlugin plugin, ProxyServer server, Logger logger, String language) {
        INSTANCE = this;
        this.plugin = plugin;
        this.logger = logger;
        this.currentLanguage = language;
    }

    public static VelocityTranslator get() {
        return INSTANCE;
    }

    public void load() {
        Path folder = plugin.getDataDirectory().resolve("lang");
        Path file = folder.resolve(currentLanguage + ".yml");

        try {
            if (!Files.exists(folder)) {
                Files.createDirectories(folder);
            }

            if (!Files.exists(file)) {
                try (InputStream in = plugin.getClass().getResourceAsStream("/lang/" + currentLanguage + ".yml")) {
                    if (in != null) {
                        Files.copy(in, file);
                    } else {
                        logger.error("lang/" + currentLanguage + ".yml introuvable dans le JAR.");
                    }
                }
            }

            Yaml yamlLoader = new Yaml();
            try (InputStream input = Files.newInputStream(file)) {
                Object loaded = yamlLoader.load(input);
                if (loaded instanceof Map<?, ?> m) {
                    yaml = convert(m);
                } else {
                    yaml = new HashMap<>();
                }
            }

            for (LangKey key : LangKey.values()) {
                if (!contains(key.getTranslationKey())) {
                    addMissingKey(file, key.getTranslationKey());
                }
            }

        } catch (Exception ex) {
            logger.error("Erreur de chargement de la langue " + currentLanguage, ex);
        }
    }

    private Map<String, Object> convert(Map<?, ?> map) {
        Map<String, Object> out = new HashMap<>();
        for (var e : map.entrySet()) {
            if (e.getValue() instanceof Map<?, ?> sub) {
                out.put(String.valueOf(e.getKey()), convert(sub));
            } else {
                out.put(String.valueOf(e.getKey()), e.getValue());
            }
        }
        return out;
    }

    private boolean contains(String path) {
        return getPath(path) != null;
    }

    private void addMissingKey(Path file, String path) throws IOException {
        Files.writeString(
                file,
                "\n" + path + ": \"__" + path.replace(".", "_") + "\"",
                java.nio.file.StandardOpenOption.APPEND
        );
    }

    public void reload() {
        String newLang = plugin.getConfig().getString("language", "fr");

        if (!newLang.equals(currentLanguage)) {
            currentLanguage = newLang;
        }

        load();
    }

    public void reloadOnlyFile() {
        load();
    }

    public String raw(String key) {
        Object o = getPath(key);
        return o == null ? "__" + key.replace(".", "_") : o.toString();
    }

    public String raw(String key, Map<String, String> placeholders) {
        String msg = raw(key);
        if (placeholders != null) {
            for (var e : placeholders.entrySet()) {
                msg = msg.replace("%" + e.getKey() + "%", e.getValue());
            }
        }
        return msg;
    }

    private Object getPath(String path) {
        String[] parts = path.split("\\.");
        Object current = yaml;

        for (String p : parts) {
            if (!(current instanceof Map)) return null;
            current = ((Map<?, ?>) current).get(p);
            if (current == null) return null;
        }
        return current;
    }

    public Path getLangFile() {
        return plugin.getDataDirectory().resolve("lang").resolve(currentLanguage + ".yml");
    }

    public String getCurrentLanguage() {
        return currentLanguage;
    }
}
