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
import fr.iban.common.manager.GlobalLoggerManager;
import fr.iban.common.manager.PlayerManager;
import fr.iban.common.teleport.SLocation;
import fr.iban.velocitycore.command.*;
import fr.iban.velocitycore.listener.*;
import fr.iban.velocitycore.manager.*;
import fr.iban.velocitycore.util.TabHook;
import org.slf4j.Logger;
import revxrsal.commands.velocity.VelocityCommandHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.TreeMap;

@Plugin(
        id = "corevelocity",
        name = "CoreVelocity",
        version = "1.0.0",
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

    private AnnoncesManager announceManager;
    private ChatManager chatManager;
    private TeleportManager teleportManager;
    private PlayerManager playerManager;
    private MessagingManager messagingManager;
    private AccountManager accountManager;

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
        playerManager = new PlayerManager();
        playerManager.clearOnlinePlayersFromDB();
        accountManager = new AccountManager(this);
        chatManager = new ChatManager(this);
        announceManager = new AnnoncesManager(this);


        EventManager eventManager = server.getEventManager();
        eventManager.register(this, new PluginMessageListener(this));
        eventManager.register(this, new ProxyJoinQuitListener(this));
        eventManager.register(this, new CoreMessageListener(this));
        eventManager.register(this, new ProxyPingListener(this));
        eventManager.register(this, new CommandListener(this));

        registerCommands();

        tabHook = new TabHook(this);
        tabHook.enable();
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        messagingManager.close();
        DbAccess.closePool();
    }

    public void registerCommands() {
        VelocityCommandHandler handler = VelocityCommandHandler.create(this, server);

        handler.register(new AnnounceCMD(this));
        handler.register(new AnnounceEventCMD(this));
        handler.register(new ChatCMD(this));
        handler.register(new CoreCMD(this));
        handler.register(new CoreCommands(this));
        handler.register(new IgnoreCommand(this));
        handler.register(new JoinEventCMD(this));
        handler.register(new MessageCMD(this));
        handler.register(new MiscellaneousCommands());
        handler.register(new MsgToggleCMD(this));
        handler.register(new ReplyCMD(this));
        handler.register(new StaffChatToggle(this));
        handler.register(new SudoCMD(this));
        handler.register(new TabCompleteCMD(this));
        handler.register(new TeleportCommands(this));
        handler.register(new TptoggleCMD(this));
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

    public AnnoncesManager getAnnounceManager() {
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

    public AccountManager getAccountManager() {
        return accountManager;
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
