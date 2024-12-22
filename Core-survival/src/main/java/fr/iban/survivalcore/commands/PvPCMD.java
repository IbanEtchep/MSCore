package fr.iban.survivalcore.commands;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.common.enums.Option;
import fr.iban.common.manager.PlayerManager;
import fr.iban.common.model.MSPlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PvPCMD implements CommandExecutor {

    private final CoreBukkitPlugin plugin;

    public PvPCMD(CoreBukkitPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(sender instanceof Player player){
            PlayerManager playerManager = plugin.getPlayerManager();

            if(args.length == 0) {
                MSPlayerProfile profile = playerManager.getProfile(player.getUniqueId());

                profile.toggleOption(Option.PVP);
                if(profile.getOption(Option.PVP)) {
                    player.sendMessage("§aPVP activé.");
                }else {
                    player.sendMessage("§cPVP desactivé.");
                }

                playerManager.saveProfile(profile);
            }else if(args.length == 1 && sender.hasPermission("servercore.pvp.others")) {
                Player target = Bukkit.getPlayer(args[0]);
                if(target != null) {
                    MSPlayerProfile profile = playerManager.getProfile(target.getUniqueId());
                    profile.toggleOption(Option.PVP);
                    if(profile.getOption(Option.PVP)) {
                        player.sendMessage("§aPVP de "+ target.getName() +" activé.");
                    }else {
                        player.sendMessage("§cPVP "+ target.getName() +" desactivé.");
                    }

                    playerManager.saveProfile(profile);
                }
            }
        }
        return false;
    }
}
