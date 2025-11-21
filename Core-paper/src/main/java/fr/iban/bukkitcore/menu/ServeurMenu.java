package fr.iban.bukkitcore.menu;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.bukkitcore.utils.ItemBuilder;
import fr.iban.bukkitcore.utils.Lang;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ServeurMenu extends Menu {

    public ServeurMenu(Player player) {
        super(player);
    }

    @Override
    public String getMenuName() {
        return Lang.get("menus.serveur.title");
    }

    @Override
    public int getRows() {
        return 1;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        if (e.getClickedInventory() == e.getView().getTopInventory() && e.getCurrentItem() != null) {
            if (e.getCurrentItem().getType() == Material.GRASS_BLOCK) {
                CoreBukkitPlugin.getInstance().getTeleportManager().teleportToSurvivalServer(player, null);
            } else if (e.getCurrentItem().getType() == Material.IRON_PICKAXE) {
                new RessourceMenu(player).open();
            }
        }
    }

    @Override
    public void setMenuItems() {
        inventory.setItem(2, new ItemBuilder(Material.GRASS_BLOCK)
                .setName(Lang.get("menus.serveur.survie-name"))
                .setLore(Lang.get("menus.serveur.survie-lore"))
                .build());

        inventory.setItem(6, new ItemBuilder(Material.IRON_PICKAXE)
                .setName(Lang.get("menus.serveur.ressources-name"))
                .setLore(Lang.get("menus.serveur.ressources-lore"))
                .build());

        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null)
                inventory.setItem(i, FILLER_GLASS);
        }
    }
}
