package fr.iban.velocitycore;

import com.google.inject.Inject;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.ChannelRegistrar;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import fr.iban.common.data.sql.DbAccess;
import fr.iban.common.data.sql.DbCredentials;
import fr.iban.common.data.sql.DbTables;
import fr.iban.common.manager.PlayerManager;
import fr.iban.common.teleport.SLocation;
import fr.iban.velocitycore.command.*;
import fr.iban.velocitycore.listener.*;
import fr.iban.velocitycore.manager.*;
import fr.iban.velocitycore.util.TabHook;
import org.slf4j.Logger;
import revxrsal.commands.Lamp;
import revxrsal.commands.velocity.VelocityLamp;
import revxrsal.commands.velocity.actor.VelocityCommandActor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.TreeMap;

import static revxrsal.commands.velocity.VelocityVisitors.brigadier;

@Plugin(
        id = "corevelocity",
        name = "CoreVelocity",
        version = "1.0.5",
        dependencies = {
                @Dependency(id = "tab", optional = true),
                @Dependency(id = "luckperms"),
                @Dependency(id = "papiproxybridge"),
        }
)
public class CoreVelocityPlugin {

    private static CoreVelocityPlugin instance;
    private final Logger logger;
    private final ProxyServer server;
    private YamlDocument config;

    private AutomatedAnnounceManager announceManager;
    private ChatManager chatManager;
    private TeleportManager teleportManager;
    private PlayerManager playerManager;
    private MessagingManager messagingManager;

    private TabHook tabHook;
    private final TreeMap<String, SLocation> currentEvents = new TreeMap<>();

    @Inject
    public CoreVelocityPlugin(Logger logger, ProxyServer server, @DataDirectory Path dataDirectory) {
        this.logger = logger;
        this.server = server;

        try {
            config = YamlDocument.create(new File(dataDirectory.toFile(), "config.yml"),
                    Objects.requireNonNull(getClass().getResourceAsStream("/config.yml")),
                    GeneralSettings.DEFAULT,
                    LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings.builder()
                            .setVersioning(new BasicVersioning("file-version"))
                            .setOptionSorting(UpdaterSettings.OptionSorting.SORT_BY_DEFAULTS).build()
            );

            config.update();
            config.save();
        } catch (IOException e) {
            logger.error("Error while loading config file", e);
            server.shutdown();
        }

        registerCommands();
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        instance = this;
        initDatabase();

        ChannelRegistrar channelRegistrar = getServer().getChannelRegistrar();
        channelRegistrar.register(MinecraftChannelIdentifier.from("proxy:chat"));
        channelRegistrar.register(MinecraftChannelIdentifier.from("proxy:annonce"));
        channelRegistrar.register(MinecraftChannelIdentifier.from("proxy:send"));

        messagingManager = new MessagingManager(this);
        messagingManager.init();
        teleportManager = new TeleportManager(this);
        playerManager = new PlayerManager(messagingManager);
        playerManager.clearOnlinePlayers();
        chatManager = new ChatManager(this);
        announceManager = new AutomatedAnnounceManager(this);


        EventManager eventManager = server.getEventManager();
        eventManager.register(this, new PluginMessageListener(this));
        eventManager.register(this, new ProxyJoinQuitListener(this));
        eventManager.register(this, new CoreMessageListener(this));
        eventManager.register(this, new ProxyPingListener(this));
        eventManager.register(this, new CommandListener(this));

        tabHook = new TabHook(this);
        tabHook.enable();
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        messagingManager.close();
        DbAccess.closePool();
    }

    public void registerCommands() {
        Lamp<VelocityCommandActor> lamp = VelocityLamp.builder(this, server).build();

        lamp.register(new AnnounceCMD(this));
        lamp.register(new AnnounceEventCMD(this));
        lamp.register(new ChatCMD(this));
        lamp.register(new CoreCMD(this));
        lamp.register(new CoreCommands(this));
        lamp.register(new IgnoreCommand(this));
        lamp.register(new JoinEventCMD(this));
        lamp.register(new MessageCMD(this));
        lamp.register(new MiscellaneousCommands());
        lamp.register(new MsgToggleCMD(this));
        lamp.register(new ReplyCMD(this));
        lamp.register(new StaffChatToggle(this));
        lamp.register(new SudoCMD(this));
        lamp.register(new TabCompleteCMD(this));
        lamp.register(new TeleportCommands(this));
        lamp.register(new TpToggleCMD(this));

        lamp.accept(brigadier(server));
    }

    public void initDatabase() {
        try {
            DbAccess.initPool(new DbCredentials(
                    config.getString("database.host"),
                    config.getString("database.user"),
                    config.getString("database.password"),
                    config.getString("database.dbname"),
                    config.getInt("database.port")));
        } catch (Exception e) {
            logger.error("Erreur lors de l'initialisation de la connexion sql.", e);
            server.shutdown();
            return;
        }

        DbTables.createTables();
    }

    public static CoreVelocityPlugin getInstance() {
        return instance;
    }

    public Logger getLogger() {
        return logger;
    }

    public AutomatedAnnounceManager getAnnounceManager() {
        return announceManager;
    }

    public ChatManager getChatManager() {
        return chatManager;
    }

    public TabHook getTabHook() {
        return tabHook;
    }

    public TeleportManager getTeleportManager() {
        return teleportManager;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public MessagingManager getMessagingManager() {
        return messagingManager;
    }

    public ProxyServer getServer() {
        return server;
    }

    public YamlDocument getConfig() {
        return config;
    }

    public String getServerName() {
        return "proxy";
    }

    public TreeMap<String, SLocation> getCurrentEvents() {
        return currentEvents;
    }

}
