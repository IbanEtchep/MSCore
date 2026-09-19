package fr.iban.velocitycore.lang;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.Route;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import fr.iban.common.lang.Lang;
import fr.iban.common.lang.LangManager;
import fr.iban.velocitycore.CoreVelocityPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class VelocityLangManager extends LangManager {

    private static final String DEFAULT_LOCALE = "en";

    private final CoreVelocityPlugin plugin;
    private final File dataDirectory;

    public VelocityLangManager(CoreVelocityPlugin plugin, File dataDirectory) {
        this.plugin = plugin;
        this.dataDirectory = dataDirectory;
        reload();
    }

    @Override
    public void reload() {
        String configured = plugin.getConfig().getString("language", DEFAULT_LOCALE);
        this.fallback = load(DEFAULT_LOCALE);
        this.active = configured.equals(DEFAULT_LOCALE) ? fallback : load(configured);

        if (active == null) {
            plugin.getLogger().warn("Language '{}' not found, falling back to '{}'.", configured, DEFAULT_LOCALE);
            this.active = fallback;
        }
    }

    private Lang load(String locale) {
        String resourcePath = "/lang/" + locale + ".yml";
        InputStream defaults = getClass().getResourceAsStream(resourcePath);
        if (defaults == null) {
            plugin.getLogger().warn("No bundled language file for '{}'.", locale);
            return null;
        }

        try {
            File file = new File(new File(dataDirectory, "lang"), locale + ".yml");
            file.getParentFile().mkdirs();
            YamlDocument document = YamlDocument.create(
                    file,
                    defaults,
                    GeneralSettings.DEFAULT,
                    LoaderSettings.builder().setAutoUpdate(false).build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings.DEFAULT
            );

            Map<String, String> entries = new HashMap<>();
            flatten(document, "", entries);
            return new Lang(locale, entries);
        } catch (IOException e) {
            plugin.getLogger().error("Failed to load language file for '{}'.", locale, e);
            return null;
        }
    }

    private void flatten(Section section, String prefix, Map<String, String> out) {
        for (Object keyObj : section.getKeys()) {
            String key = keyObj.toString();
            String path = prefix.isEmpty() ? key : prefix + "." + key;
            Route route = Route.fromSingleKey(key);
            if (section.isSection(route)) {
                flatten(section.getSection(route), path, out);
            } else {
                Object value = section.get(route);
                if (value != null) {
                    out.put(path, value.toString());
                }
            }
        }
    }
}
