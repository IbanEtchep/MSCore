package fr.iban.bukkitcore.lang;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.libs.org.snakeyaml.engine.v2.common.ScalarStyle;
import dev.dejvokep.boostedyaml.libs.org.snakeyaml.engine.v2.nodes.Tag;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import fr.iban.bukkitcore.CoreBukkitPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.logging.Logger;

public class BukkitTranslator {

    private static BukkitTranslator INSTANCE;

    private final Logger logger;
    private final File langFolder;
    private String currentLanguage;
    private YamlDocument messages;

    public BukkitTranslator(CoreBukkitPlugin plugin, String language) {
        INSTANCE = this;
        this.logger = plugin.getLogger();
        this.langFolder = new File(plugin.getDataFolder(), "lang");
        this.currentLanguage = language;
    }

    public static BukkitTranslator get() {
        return INSTANCE;
    }

    public void load(CoreBukkitPlugin plugin) {
        if (!langFolder.exists()) langFolder.mkdirs();

        File file = new File(langFolder, currentLanguage + ".yml");
        InputStream defaults = plugin.getResource("lang/" + currentLanguage + ".yml");

        try {
            messages = YamlDocument.create(
                    file,
                    defaults,
                    GeneralSettings.DEFAULT,
                    LoaderSettings.DEFAULT,
                    DumperSettings.builder()
                            .setScalarFormatter((tag, value, role, defaultStyle) -> {
                                if (tag != Tag.STR) return defaultStyle;
                                if (value.contains("\n")) return ScalarStyle.LITERAL;
                                return defaultStyle;
                            }).build(),
                    UpdaterSettings.builder()
                            .setVersioning(new BasicVersioning("file_version"))
                            .build()
            );

            messages.update();

            for (LangKey key : LangKey.values()) {
                if (!messages.contains(key.getTranslationKey())) {
                    messages.set(key.getTranslationKey(), "__" + key.getTranslationKey().replace(".", "_"));
                    logger.warning("Missing lang key: " + key.getTranslationKey());
                }
            }

            messages.save();

        } catch (IOException ex) {
            logger.severe("Failed to load lang file: " + ex.getMessage());
        }
    }

    public void reload(CoreBukkitPlugin plugin) {
        String newLang = plugin.getConfig().getString("language", "fr");
        if (!newLang.equals(currentLanguage)) {
            currentLanguage = newLang;
        }
        load(plugin);
    }

    public void reloadOnlyFile(CoreBukkitPlugin plugin) {
        load(plugin);
    }

    public String raw(String key) {
        return messages.getString(key, "__" + key.replace(".", "_"));
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

    public String getCurrentLanguage() {
        return currentLanguage;
    }
}
