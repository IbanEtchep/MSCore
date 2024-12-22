package fr.iban.survivalcore.listeners;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.bukkitcore.manager.AccountManager;
import fr.iban.bukkitcore.utils.ChatUtils;
import fr.iban.common.data.Account;
import fr.iban.common.data.Option;
import fr.iban.survivalcore.SurvivalCorePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class EntityDeathListener implements Listener {

    private final SurvivalCorePlugin plugin;

    public EntityDeathListener(SurvivalCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        e.deathMessage(null);
        Player killer = e.getEntity().getKiller();
        Player player = e.getEntity();
        String message = "☠ " + player.getName() + " ";

        EntityDamageEvent damageCause = e.getEntity().getLastDamageCause();
        if (damageCause == null)
            return;

        switch (damageCause.getCause()) {
            case SUICIDE:
                message += "s'est suicidé !";
                break;
            case BLOCK_EXPLOSION:
                message += "s'est fait broyer par de la TNT";
                break;
            case CONTACT:
                message += "a essayé de piquer un cactus !";
                break;
            case DROWNING:
                message += "s'est noyé !";
                break;
            case ENTITY_EXPLOSION:
                message += "s'est fait exploser par un Creeper !";
                break;
            case FIRE_TICK:
                message += "a brûlé.";
                break;
            case LAVA:
                message += "a essayé de nager dans la lave !";
                break;
            case MAGIC:
                message += "a été tué par une potion!";
                break;
            case POISON:
                message += "a été empoisonné!";
                break;
            case PROJECTILE:
                if (killer != null)
                    message += "s'est fait sniper par " + killer.getName() + " !";
                else
                    message += "s'est pris une flèche dans le crâne.";
                break;
            case STARVATION:
                message += "est mort de faim.";
                break;
            case SUFFOCATION:
                message += "a suffoqué dans un mur.";
                break;
            case VOID:
                message += "a disparu dans le néant...";
                break;
            case FALL:
                message += "a fait une terrible chute !";
                break;
            case WITHER:
                message += "a été tué par un Wither.";
                break;
            case LIGHTNING:
                message += "a été abattu par la colère de Zeus!";
                break;
            case HOT_FLOOR:
                message += "a marché sur un bloc très chaud !";
                break;
            case FLY_INTO_WALL:
                message += "a frappé le mur à une vitesse supersonique !";
                break;
            case FALLING_BLOCK:
                message += "s'est fait écraser par une entité tombée du ciel !";
                break;
            case FIRE:
                message += "a joué avec le feu mais s'est brûlé.";
                break;
            case DRAGON_BREATH:
                message += "a subi la colère du Dragon !";
                break;
            case THORNS:
                if (damageCause instanceof EntityDamageByEntityEvent entity) {
                    if (entity.getDamager() instanceof Player) {
                        killer = (Player) entity.getDamager();
                        message += "s'est tué en frappant " + killer.getName() + " !";
                    } else if (entity.getDamager() instanceof Mob) {
                        message += "s'est tué en frappant un " + entity.getDamager().getType().toString().toLowerCase().replace("_", " ") + " !";
                    } else if (entity.getDamager().getType() == EntityType.ARMOR_STAND) {
                        message += "s'est tué en frappant un porte armure...";
                    }
                }
                break;
            case ENTITY_ATTACK:
                if (damageCause instanceof EntityDamageByEntityEvent entity) {
                    if (entity.getDamager() instanceof Player) {
                        killer = (Player) entity.getDamager();
                        Material weapon = killer.getInventory().getItemInMainHand().getType();
                        if (weapon == Material.AIR) {
                            message += "s'est fait boxer par " + killer.getName() + " !";
                        } else {
                            message += "s'est fait assassiner par " + killer.getName() + " !";
                        }
                    } else if (entity.getDamager() instanceof Mob) {
                        if(entity.getDamager().getType() == EntityType.WARDEN) {
                            message += "s'est fait atomiser par un Warden !";
                        }else{
                            message += "s'est fait tuer par un " + entity.getDamager().getType().toString().toLowerCase().replace("_", " ") + " !";
                        }
                    }
                }
                break;
            default:
                message += "est mort.";
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            AccountManager accountManager = CoreBukkitPlugin.getInstance().getAccountManager();
            Account account = accountManager.getAccount(p.getUniqueId());
            if (account.getOption(Option.DEATH_MESSAGE)) {
                if (!account.getIgnoredPlayers().contains(player.getUniqueId())) {
                    p.sendMessage(message);
                }
            }
        }

        SurvivalCorePlugin.getInstance().getLogger().info(message);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        CoreBukkitPlugin core = CoreBukkitPlugin.getInstance();
        Player player = e.getEntity();
        Location location = player.getLocation();
        player.sendMessage("§3§lVous êtes mort à la position suivante: \n" +
                "§bServeur : §f" + core.getServerName() + "\n" +
                "§bMonde : §f" + location.getWorld().getName() + "\n" +
                "§bCoordonnées : X : §f" + (int) location.getX() +
                " §bY : §f" + (int) location.getY() +
                " §bZ : §f" + (int) location.getZ());

        if(core.getServerName().equalsIgnoreCase("ressources")) {
            String miniMessageText = "<aqua><bold>Vous pouvez vous téléporter à la position de votre dernière téléportation aléatoire en cliquant sur ce message ou en exécutant la commande /lastrtp."
                    + "<hover:show_text:'<bold>Clic ici !'>"
                    + "<click:run_command:/lastrtp>";

            Component message = MiniMessage.miniMessage().deserialize(miniMessageText);
            player.sendMessage(message);
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        boolean disableEnemyDrops = plugin.getConfig().getBoolean("loots.disable-enemy-drops", false);
        Entity entity = event.getEntity();

        if(disableEnemyDrops && entity instanceof Enemy) {
            event.getDrops().clear();
        }
    }
}