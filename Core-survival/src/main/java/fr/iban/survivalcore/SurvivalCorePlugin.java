package fr.iban.survivalcore;

import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.PlatformScheduler;
import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.bukkitcore.utils.PluginMessageHelper;
import fr.iban.survivalcore.commands.*;
import fr.iban.survivalcore.listeners.*;
import fr.iban.survivalcore.manager.AnnounceManager;
import fr.iban.survivalcore.utils.HourlyReward;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.commands.bukkit.BukkitCommandHandler;

public final class SurvivalCorePlugin extends JavaPlugin implements Listener {

    private static SurvivalCorePlugin instance;
    private Economy econ;
    private AnnounceManager announceManager;
    private FoliaLib foliaLib;


    @Override
    public void onEnable() {
        instance = this;
        PluginMessageHelper.registerChannels(this);

        foliaLib = new FoliaLib(this);

        saveDefaultConfig();
        setupEconomy();

        registerEvents(
                new EntityDeathListener(this),
                new CommandListener(),
                new VillagerEvents(this),
                new RaidTriggerListener(this),
                new PortalListeners(this),
                new DamageListeners(),
                new InteractListeners(),
                new ServiceListeners(this),
                new JoinQuitListeners(this),
                new FishingListener(),
                new CoreMessageListener(this)
        );

        Plugin betterrtp = getServer().getPluginManager().getPlugin("BetterRTP");
        if (betterrtp != null) {
            if (betterrtp.isEnabled()) {
                getLogger().info("Listening BetterRTP");
                getServer().getPluginManager().registerEvents(new RTPListeners(), this);
            }
        }

        registerCommands();

        HourlyReward hourlyReward = new HourlyReward(this);
        hourlyReward.init();

        announceManager = new AnnounceManager(this);
    }

    public static SurvivalCorePlugin getInstance() {
        return instance;
    }

    private void registerCommands() {
        getCommand("dolphin").setExecutor(new DolphinCMD());
        getCommand("pvp").setExecutor(new PvPCMD(CoreBukkitPlugin.getInstance()));

        BukkitCommandHandler commandHandler = BukkitCommandHandler.create(this);
        commandHandler.accept(CoreBukkitPlugin.getInstance().getCommandHandlerVisitor());

        commandHandler.register(new RepairCMD(this));
        commandHandler.register(new FeedCMD(this));
        commandHandler.register(new ShowGroupsCMD());
        commandHandler.register(new AnnounceCMD(this));
        commandHandler.registerBrigadier();
    }

    public void registerEvents(Listener... listeners) {
        for (Listener listener : listeners) {
            getServer().getPluginManager().registerEvents(listener, this);
        }
    }

    public void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            getLogger().info("No economy provider found.");
            return;
        }
        getLogger().info("Using " + rsp.getProvider().getName() + " economy.");
        econ = rsp.getProvider();
    }

    public Economy getEconomy() {
        return econ;
    }

    public AnnounceManager getAnnounceManager() {
        return announceManager;
    }

    public PlatformScheduler getScheduler() {
        return foliaLib.getScheduler();
    }
}