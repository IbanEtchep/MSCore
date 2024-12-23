package fr.iban.survivalcore.commands;

import com.earth2me.essentials.utils.DateUtil;
import fr.iban.survivalcore.SurvivalCorePlugin;
import fr.iban.survivalcore.event.ItemRepairEvent;
import fr.iban.survivalcore.tools.SpecialTools;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.CommandPlaceholder;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.*;

@Command("repair")
public class RepairCMD {

    private final SurvivalCorePlugin plugin;

    private final Map<UUID, Long> repairCooldowns = new HashMap<>();
    private final Map<UUID, Long> repairAllCooldowns = new HashMap<>();

    public RepairCMD(SurvivalCorePlugin plugin) {
        this.plugin = plugin;
    }

    @CommandPlaceholder
    public void repair(Player player) {
        repairHand(player);
    }

    @Subcommand("hand")
    @CommandPermission("servercore.repair")
    public void repairHand(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();

        if (hasRepairCooldown(player)) {
            return;
        }

        if (isRepairable(item) && repairItem(item)) {
            player.sendMessage(ChatColor.GOLD + "§aRéparation effectuée.");
            repairCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        } else {
            player.sendMessage(ChatColor.RED + "Erreur: " + ChatColor.DARK_RED + "Cet item n'est pas réparable");
        }
    }

    @Subcommand("all")
    @CommandPermission("servercore.repairall")
    public void repairAll(Player player) {
        if (!hasRepairAllCooldown(player)) {
            for (ItemStack item : player.getInventory().getContents()) {
                if (isRepairable(item)) {
                    repairItem(item);
                }
            }
            for (ItemStack item : player.getInventory().getArmorContents()) {
                if (isRepairable(item)) {
                    repairItem(item);
                }
            }
            repairAllCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
            player.sendMessage(ChatColor.GOLD + "§aRéparations effectuées.");
        }
    }

    private boolean isRepairable(ItemStack item) {
        if (item != null && item.getItemMeta() instanceof Damageable damageable) {
            if (damageable.getDamage() == 0) {
                return false;
            }
            return !SpecialTools.is3x3Pickaxe(item) || Objects.requireNonNull(item.getItemMeta().getLore()).contains("§c§l[ITEM LEGENDAIRE]");
        }
        return false;
    }

    public boolean repairItem(ItemStack item) {
        if (item.getItemMeta() instanceof Damageable damageable) {
            ItemRepairEvent event = new ItemRepairEvent(item);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                damageable.setDamage(0);
                item.setItemMeta(damageable);
                return true;
            }
        }
        return false;
    }

    private boolean hasRepairAllCooldown(Player player) {
        if (player.hasPermission("servercore.repairall.bypasscooldown")) {
            return false;
        }
        if (repairAllCooldowns.containsKey(player.getUniqueId())) {
            int cooldownTime = getCooldownFromConfig(player, "repairall") * 1000;
            long lastRep = repairAllCooldowns.get(player.getUniqueId());
            if (System.currentTimeMillis() - lastRep > cooldownTime) {
                repairAllCooldowns.remove(player.getUniqueId());
                return false;
            } else {
                player.sendMessage("§cVous pourrez à nouveau réparer un item dans " + DateUtil.formatDateDiff(lastRep + cooldownTime) + " secondes.");
                return true;
            }
        }
        return false;
    }

    private boolean hasRepairCooldown(Player player) {
        if (player.hasPermission("servercore.repairall.bypasscooldown")) {
            return false;
        }
        if (repairCooldowns.containsKey(player.getUniqueId())) {
            int cooldownTime = getCooldownFromConfig(player, "repair") * 1000;
            long lastRep = repairCooldowns.get(player.getUniqueId());
            if (System.currentTimeMillis() - lastRep > cooldownTime) {
                repairCooldowns.remove(player.getUniqueId());
                return false;
            } else {
                long remain = cooldownTime - (System.currentTimeMillis() - lastRep);
                player.sendMessage("§cVous pourrez à nouveau réparer un item dans " + remain / 1000 + " secondes.");
                return true;
            }
        }
        return false;
    }

    private int getCooldownFromConfig(Player player, String command) {
        int minCooldown = Integer.MAX_VALUE;
        List<String> cooldowns = plugin.getConfig().getStringList("cooldowns." + command + ".permissions");
        for (String cooldownString : cooldowns) {
            String[] splitted = cooldownString.split(":");
            String permission = splitted[0];
            int cooldownTime = Integer.parseInt(splitted[1]);
            if (player.hasPermission(permission) && minCooldown > cooldownTime) {
                minCooldown = cooldownTime;
            }
        }
        return minCooldown;
    }
}
