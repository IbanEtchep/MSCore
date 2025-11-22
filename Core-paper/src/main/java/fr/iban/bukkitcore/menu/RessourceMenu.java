package fr.iban.bukkitcore.menu;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.bukkitcore.lang.LangKey;
import fr.iban.bukkitcore.lang.MessageBuilder;
import fr.iban.bukkitcore.manager.RessourcesWorldManager;
import fr.iban.bukkitcore.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class RessourceMenu extends Menu {

    public RessourceMenu(Player player) {
        super(player);
    }

    @Override
    public String getMenuName() {
        return MessageBuilder.translatable(LangKey.MENUS_RESSOURCES_TITLE).toLegacy();
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        if(e.getClickedInventory() == e.getView().getTopInventory() && e.getCurrentItem() != null) {
            RessourcesWorldManager ressourcesWorldManager = CoreBukkitPlugin.getInstance().getRessourcesWorldManager();
            if(e.getCurrentItem().getType() == Material.GRASS_BLOCK) {
                ressourcesWorldManager.randomTpResourceWorld(player, "resource_world");
            } else if (e.getCurrentItem().getType() == Material.NETHERRACK) {
                ressourcesWorldManager.randomTpResourceWorld(player, "resource_nether");
            } else if (e.getCurrentItem().getType() == Material.END_STONE) {
                ressourcesWorldManager.randomTpResourceWorld(player, "resource_end");
            }
        }
    }

    @Override
    public void setMenuItems() {
        inventory.setItem(2, new ItemBuilder(Material.GRASS_BLOCK)
                .setName(MessageBuilder.translatable(LangKey.MENUS_RESSOURCES_NORMAL_NAME).toLegacy())
                .setLore(MessageBuilder.translatable(LangKey.MENUS_RESSOURCES_NORMAL_LORE).toLegacy())
                .build());

        inventory.setItem(4, new ItemBuilder(Material.NETHERRACK)
                .setName(MessageBuilder.translatable(LangKey.MENUS_RESSOURCES_NETHER_NAME).toLegacy())
                .setLore(MessageBuilder.translatable(LangKey.MENUS_RESSOURCES_NETHER_LORE).toLegacy())
                .build());

        inventory.setItem(6, new ItemBuilder(Material.END_STONE)
                .setName(MessageBuilder.translatable(LangKey.MENUS_RESSOURCES_END_NAME).toLegacy())
                .setLore(MessageBuilder.translatable(LangKey.MENUS_RESSOURCES_END_LORE).toLegacy())
                .build());

        for(int i = 0 ; i < inventory.getSize() ; i++) {
            if(inventory.getItem(i) == null)
                inventory.setItem(i, FILLER_GLASS);
        }
    }

    @Override
    public int getRows() {
        return 1;
    }
}
