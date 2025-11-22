package fr.iban.survivalcore.lang;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.libs.org.snakeyaml.engine.v2.common.ScalarStyle;
import dev.dejvokep.boostedyaml.libs.org.snakeyaml.engine.v2.nodes.Tag;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import fr.iban.survivalcore.SurvivalCorePlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.logging.Logger;

public class SurvivalTranslator {

    private static SurvivalTranslator INSTANCE;

    private final Logger logger;
    private final SurvivalCorePlugin plugin;
    private final File langFolder;
    private String language;
    private YamlDocument messages;

    public SurvivalTranslator(SurvivalCorePlugin plugin, String language) {
        INSTANCE = this;
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.langFolder = new File(plugin.getDataFolder(), "lang");
        this.language = language;
    }

    public static SurvivalTranslator get() {
        return INSTANCE;
    }

    public void load() {
        if (!langFolder.exists()) langFolder.mkdirs();

        File file = new File(langFolder, language + ".yml");
        InputStream defaults = plugin.getResource("lang/" + language + ".yml");

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
                    UpdaterSettings.builder().setVersioning(new BasicVersioning("file_version")).build()
            );

            messages.update();

            for (LangKey key : LangKey.values()) {
                if (!messages.contains(key.getTranslationKey())) {
                    messages.set(key.getTranslationKey(), "__missing_" + key.getTranslationKey());
                }
            }

            messages.save();

        } catch (IOException ex) {
            logger.severe("Failed to load lang file: " + ex.getMessage());
        }
    }

    public void reload() {
        String newLang = plugin.getConfig().getString("language", "fr");
        if (!newLang.equals(language)) {
            language = newLang;
        }
        load();
    }

    public void reloadOnlyFile() {
        load();
    }

    public String raw(String key) {
        return messages.getString(key, "__missing_" + key);
    }

    public String raw(String key, Map<String, String> placeholders) {
        String out = raw(key);
        if (placeholders != null) {
            for (var entry : placeholders.entrySet()) {
                out = out.replace("%" + entry.getKey() + "%", entry.getValue());
            }
        }
        return out;
    }
}
