package fr.iban.survivalcore.utils;

import fr.iban.survivalcore.SurvivalCorePlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Lang {

    private static FileConfiguration config;

    public static void init(SurvivalCorePlugin plugin) {
        File folder = plugin.getDataFolder();
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File file = new File(folder, "translation.yml");

        if (!file.exists()) {
            plugin.saveResource("translation.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(file);
    }

    public static String get(String path) {
        String raw = config.getString(path, "§cMissing lang: " + path);
        return HexColor.translateColorCodes(raw);
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

    public static List<String> getList(String path) {
        List<String> list = config.getStringList(path);
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        return HexColor.translateColorCodes(list);
    }
}
