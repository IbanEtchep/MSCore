package fr.iban.bukkitcore.menu;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.bukkitcore.manager.RessourcesWorldManager;
import fr.iban.bukkitcore.utils.ItemBuilder;
import fr.iban.bukkitcore.utils.Lang;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class RessourceMenu extends Menu {

	public RessourceMenu(Player player) {
		super(player);
	}

	@Override
	public String getMenuName() {
		return Lang.get("menus.ressources.title");
	}

	@Override
	public void handleMenu(InventoryClickEvent e) {
		if(e.getClickedInventory() == e.getView().getTopInventory() && e.getCurrentItem() != null) {
			RessourcesWorldManager ressourcesWorldManager = CoreBukkitPlugin.getInstance().getRessourcesWorldManager();
			if(e.getCurrentItem().getType() == Material.GRASS_BLOCK) {
				ressourcesWorldManager.randomTpResourceWorld(player, "resource_world");
			}else if (e.getCurrentItem().getType() == Material.NETHERRACK) {
				ressourcesWorldManager.randomTpResourceWorld(player, "resource_nether");
			}else if (e.getCurrentItem().getType() == Material.END_STONE) {
				ressourcesWorldManager.randomTpResourceWorld(player, "resource_end");
			}
		}
	}

	@Override
	public void setMenuItems() {
		inventory.setItem(2, new ItemBuilder(Material.GRASS_BLOCK)
				.setName(Lang.get("menus.ressources.normal.name"))
				.setLore(Lang.get("menus.ressources.normal.lore"))
				.build());

		inventory.setItem(4, new ItemBuilder(Material.NETHERRACK)
				.setName(Lang.get("menus.ressources.nether.name"))
				.setLore(Lang.get("menus.ressources.nether.lore"))
				.build());

		inventory.setItem(6, new ItemBuilder(Material.END_STONE)
				.setName(Lang.get("menus.ressources.end.name"))
				.setLore(Lang.get("menus.ressources.end.lore"))
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
