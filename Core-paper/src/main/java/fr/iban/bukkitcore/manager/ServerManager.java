package fr.iban.bukkitcore.manager;

import fr.iban.bukkitcore.CoreBukkitPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ServerManager {

    private final CoreBukkitPlugin plugin;
    private final List<String> survivalServers;
    private final String defaultSurvivalServer;


    public ServerManager(CoreBukkitPlugin plugin) {
        this.plugin = plugin;
        this.survivalServers = plugin.getConfig().getStringList("survival-servers");
        this.defaultSurvivalServer = plugin.getConfig().getString("survie-servername", "survie");
    }

    public List<String> getSurvivalServers() {
        return survivalServers;
    }

    public String getDefaultSurvivalServer() {
        return defaultSurvivalServer;
    }

    public boolean isSurvivalServer() {
        return plugin.getServerName().toLowerCase().startsWith("survie");
    }
}
