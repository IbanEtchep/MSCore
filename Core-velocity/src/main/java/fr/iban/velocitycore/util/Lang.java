package fr.iban.velocitycore.util;

import com.velocitypowered.api.proxy.ProxyServer;
import fr.iban.velocitycore.CoreVelocityPlugin;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Lang {

    private static Map<String, Object> config = new HashMap<>();

    public static void init(CoreVelocityPlugin plugin, ProxyServer server, Logger logger) {

        Path dataFolder = plugin.getDataDirectory();
        Path langFile = dataFolder.resolve("translation.yml");

        try {
            if (!Files.exists(dataFolder)) {
                Files.createDirectories(dataFolder);
            }

            if (!Files.exists(langFile)) {
                try (InputStream in = plugin.getClass().getResourceAsStream("/translation.yml")) {
                    if (in != null) {
                        Files.copy(in, langFile);
                        logger.info("Création du fichier translation.yml");
                    } else {
                        logger.error("Impossible de trouver translation.yml dans les ressources !");
                    }
                }
            }

            Yaml yaml = new Yaml();
            try (InputStream in = Files.newInputStream(langFile)) {
                config = yaml.load(in);
                if (config == null) {
                    config = new HashMap<>();
                }
            }

            logger.info("Fichier de langue chargé avec succès.");

        } catch (IOException e) {
            logger.error("Erreur lors du chargement de translation.yml", e);
        }
    }

    public static String get(String path) {
        Object result = getPath(path);
        if (result == null) {
            return "§cMissing lang: " + path;
        }
        return translateColorCodes(result.toString());
    }

    public static String get(String path, Map<String, String> placeholders) {
        String msg = get(path);
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                msg = msg.replace("%" + entry.getKey() + "%", entry.getValue());
            }
        }
        return msg;
    }

    @SuppressWarnings("unchecked")
    public static List<String> getList(String path) {
        Object result = getPath(path);
        if (result instanceof List<?>) {
            List<String> raw = (List<String>) result;
            List<String> out = new ArrayList<>();
            for (String line : raw) {
                out.add(translateColorCodes(line));
            }
            return out;
        }
        return new ArrayList<>();
    }

    private static Object getPath(String path) {
        String[] keys = path.split("\\.");
        Object current = config;

        for (String key : keys) {
            if (!(current instanceof Map)) {
                return null;
            }
            current = ((Map<?, ?>) current).get(key);
            if (current == null) {
                return null;
            }
        }

        return current;
    }

    private static String translateColorCodes(String msg) {
        return msg.replace("&", "§");
    }
}
