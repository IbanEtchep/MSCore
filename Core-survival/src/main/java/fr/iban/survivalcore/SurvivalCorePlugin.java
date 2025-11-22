package fr.iban.survivalcore;

import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.PlatformScheduler;
import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.bukkitcore.commands.CoreCommandHandlerVisitor;
import fr.iban.bukkitcore.utils.PluginMessageHelper;
import fr.iban.survivalcore.commands.*;
import fr.iban.survivalcore.lang.SurvivalTranslator;
import fr.iban.survivalcore.listeners.*;
import fr.iban.survivalcore.manager.AnnounceManager;
import fr.iban.survivalcore.utils.HourlyReward;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.commands.Lamp;
import revxrsal.commands.bukkit.BukkitLamp;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;

public final class SurvivalCorePlugin extends JavaPlugin implements Listener {

    private static SurvivalCorePlugin instance;
    private Economy econ;
    private AnnounceManager announceManager;
    private FoliaLib foliaLib;
    private SurvivalTranslator translator;

    @Override
    public void onEnable() {
        instance = this;
        PluginMessageHelper.registerChannels(this);

        foliaLib = new FoliaLib(this);

        saveDefaultConfig();
        setupEconomy();

        String lang = getConfig().getString("language", "fr");

        translator = new SurvivalTranslator(this, lang);
        translator.load();

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

        Lamp.Builder<BukkitCommandActor> lampBuilder = BukkitLamp.builder(this);
        new CoreCommandHandlerVisitor(CoreBukkitPlugin.getInstance()).visitor().visit(lampBuilder);
        Lamp<BukkitCommandActor> lamp = lampBuilder.build();

        lamp.register(new DolphinCMD());
        lamp.register(new PvPCMD(CoreBukkitPlugin.getInstance()));
        lamp.register(new RepairCMD(this));
        lamp.register(new FeedCMD(this));
        lamp.register(new ShowGroupsCMD());
        lamp.register(new AnnounceCMD(this));
        lamp.register(new SurvivalCoreCMD(this));
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

    public SurvivalTranslator getTranslator() {
        return translator;
    }
}
