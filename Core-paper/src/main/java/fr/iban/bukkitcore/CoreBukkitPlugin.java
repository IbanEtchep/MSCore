package fr.iban.bukkitcore;

import com.earth2me.essentials.Essentials;
import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.PlatformScheduler;
import fr.iban.bukkitcore.commands.*;
import fr.iban.bukkitcore.listeners.*;
import fr.iban.bukkitcore.manager.*;
import fr.iban.bukkitcore.plan.PlanDataManager;
import fr.iban.bukkitcore.rewards.RewardsDAO;
import fr.iban.bukkitcore.utils.PluginMessageHelper;
import fr.iban.bukkitcore.utils.TextCallback;
import fr.iban.common.data.sql.DbAccess;
import fr.iban.common.data.sql.DbCredentials;
import fr.iban.common.manager.GlobalLoggerManager;
import fr.iban.common.manager.TrustedCommandsManager;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.commands.Lamp;
import revxrsal.commands.bukkit.BukkitLamp;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class CoreBukkitPlugin extends JavaPlugin {

    private static CoreBukkitPlugin instance;
    private FoliaLib foliaLib;
    private String serverName;
    private TeleportManager teleportManager;
    private Map<UUID, TextCallback> textInputs;
    private Essentials essentials;
    private RessourcesWorldManager ressourcesWorldManager;
    private MessagingManager messagingManager;
    private BukkitPlayerManager playerManager;
    private TrustedCommandsManager trustedCommandManager;
    private BukkitTrustedUserManager trustedUserManager;
    private ApprovalManager approvalManager;
    private PlanDataManager planDataManager;
    private ServerManager serverManager;

    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        GlobalLoggerManager.initLogger();

        this.foliaLib = new FoliaLib(this);
        this.serverName = getConfig().getString("servername");

        if (getServer().getPluginManager().isPluginEnabled("Essentials")) {
            essentials = (Essentials) getServer().getPluginManager().getPlugin("Essentials");
            getServer().getPluginManager().registerEvents(new EssentialsListeners(this), this);
        }

        try {
            DbAccess.initPool(new DbCredentials(getConfig().getString("database.host"), getConfig().getString("database.user"), getConfig().getString("database.password"), getConfig().getString("database.dbname"), getConfig().getInt("database.port")));
        } catch (Exception e) {
            getLogger().severe("Erreur lors de l'initialisation de la connexion sql.");
            Bukkit.shutdown();
        }

        RewardsDAO.createTables();

        textInputs = new HashMap<>();

        this.teleportManager = new TeleportManager(this);
        this.ressourcesWorldManager = new RessourcesWorldManager(this);
        this.messagingManager = new MessagingManager(this);
        this.trustedCommandManager = new TrustedCommandsManager();
        foliaLib.getScheduler().runAsync(task -> getTrustedCommandManager().loadTrustedCommands());
        messagingManager.init();
        this.playerManager = new BukkitPlayerManager(messagingManager);
        this.trustedUserManager = new BukkitTrustedUserManager(this);
        this.approvalManager = new ApprovalManager(this, messagingManager, trustedUserManager);
        this.planDataManager = new PlanDataManager(this);
        this.serverManager = new ServerManager(this);

        registerListeners(
                new HeadDatabaseListener(),
                new InventoryListener(),
                new AsyncChatListener(this),
                new JoinQuitListeners(this),
                new PlayerMoveListener(this),
                new DeathListener(this),
                new CommandsListener(this),
                new CoreMessageListener(this),
                new TeleportListener()
        );

        registerCommands();

        PluginMessageHelper.registerChannels(this);
    }

    @Override
    public void onDisable() {
        GlobalLoggerManager.shutdownLogger();
        messagingManager.close();
        DbAccess.closePool();
    }

    private void registerCommands() {
        CoreCommandHandlerVisitor coreCommandHandlerVisitor = new CoreCommandHandlerVisitor(this);

        Lamp.Builder<BukkitCommandActor> lampBuilder = BukkitLamp.builder(this);
        lampBuilder.accept(coreCommandHandlerVisitor.visitor());
        Lamp<BukkitCommandActor> lamp = lampBuilder.build();

        lamp.register(new TeleportCommands(this));
        lamp.register(new TrustCommandsCMD(this));
        lamp.register(new ServerSwitchCommands(this));
        lamp.register(new CoreCMD(this));
        lamp.register(new ActionBarCMD(this));
        lamp.register(new BungeeBroadcastCMD(this));

        getCommand("options").setExecutor(new OptionsCMD());
        getCommand("recompenses").setExecutor(new RecompensesCMD(this));
        getCommand("recompenses").setTabCompleter(new RecompensesCMD(this));
    }

    private void registerListeners(Listener... listeners) {

        PluginManager pm = Bukkit.getPluginManager();

        for (Listener listener : listeners) {
            pm.registerEvents(listener, this);
        }

    }

    public static CoreBukkitPlugin getInstance() {
        return instance;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public Map<UUID, TextCallback> getTextInputs() {
        return textInputs;
    }

    public TeleportManager getTeleportManager() {
        return teleportManager;
    }

    public Essentials getEssentials() {
        return essentials;
    }

    public RessourcesWorldManager getRessourcesWorldManager() {
        return ressourcesWorldManager;
    }

    public TrustedCommandsManager getTrustedCommandManager() {
        return trustedCommandManager;
    }

    public MessagingManager getMessagingManager() {
        return messagingManager;
    }

    public BukkitPlayerManager getPlayerManager() {
        return playerManager;
    }

    public BukkitTrustedUserManager getTrustedUserManager() {
        return trustedUserManager;
    }

    public ApprovalManager getApprovalManager() {
        return approvalManager;
    }

    public PlanDataManager getPlanDataManager() {
        return planDataManager;
    }

    public ServerManager getServerManager() {
        return serverManager;
    }

    public PlatformScheduler getScheduler() {
        return foliaLib.getScheduler();
    }
}