package fr.iban.bukkitcore.lang;

import fr.iban.common.lang.Lang;
import fr.iban.common.lang.LangManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class BukkitLangManager extends LangManager {

    private static final String DEFAULT_LOCALE = "en";

    private final JavaPlugin plugin;

    public BukkitLangManager(JavaPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    @Override
    public void reload() {
        extractIfMissing(DEFAULT_LOCALE);
        String configured = plugin.getConfig().getString("language", DEFAULT_LOCALE);
        if (!configured.equals(DEFAULT_LOCALE)) {
            extractIfMissing(configured);
        }

        this.fallback = load(DEFAULT_LOCALE);
        this.active = configured.equals(DEFAULT_LOCALE) ? fallback : load(configured);

        if (active == null) {
            plugin.getLogger().warning("Language '" + configured + "' not found, falling back to '" + DEFAULT_LOCALE + "'.");
            this.active = fallback;
        }
    }

    private void extractIfMissing(String locale) {
        String resourcePath = "lang/" + locale + ".yml";
        File target = new File(plugin.getDataFolder(), resourcePath);
        if (target.exists()) {
            return;
        }
        if (plugin.getResource(resourcePath) == null) {
            return;
        }
        plugin.saveResource(resourcePath, false);
    }

    private Lang load(String locale) {
        File file = new File(plugin.getDataFolder(), "lang/" + locale + ".yml");
        if (!file.exists()) {
            plugin.getLogger().log(Level.WARNING, "Missing language file: " + file.getPath());
            return null;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        Map<String, String> entries = new HashMap<>();
        flatten(config, "", entries);
        return new Lang(locale, entries);
    }

    private void flatten(ConfigurationSection section, String prefix, Map<String, String> out) {
        for (String key : section.getKeys(false)) {
            String path = prefix.isEmpty() ? key : prefix + "." + key;
            Object value = section.get(key);
            if (value instanceof ConfigurationSection child) {
                flatten(child, path, out);
            } else if (value != null) {
                out.put(path, value.toString());
            }
        }
    }
}
