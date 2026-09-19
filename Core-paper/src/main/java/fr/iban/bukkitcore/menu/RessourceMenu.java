package fr.iban.bukkitcore.menu;

import fr.iban.bukkitcore.CoreBukkitPlugin;
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
		return "§dSélectionnez un monde :";
	}

	@Override
	public void handleMenu(InventoryClickEvent e) {
		if(e.getClickedInventory() == e.getView().getTopInventory() && e.getCurrentItem() != null) {
			RessourcesWorldManager ressourcesWorldManager = CoreBukkitPlugin.getInstance().getRessourcesWorldManager();
			if(e.getCurrentItem().getType() == Material.GRASS_BLOCK) {
				ressourcesWorldManager.randomTpResourceWorld(player, ressourcesWorldManager.getOverworldName());
			}else if (e.getCurrentItem().getType() == Material.NETHERRACK) {
				ressourcesWorldManager.randomTpResourceWorld(player, ressourcesWorldManager.getNetherName());
			}else if (e.getCurrentItem().getType() == Material.END_STONE) {
				ressourcesWorldManager.randomTpResourceWorld(player, ressourcesWorldManager.getEndName());
			}
		}
	}

	@Override
	public void setMenuItems() {
		inventory.setItem(2, new ItemBuilder(Material.GRASS_BLOCK).setName("§2§lNormal").setLore("§aCliquez pour rejoindre le monde ressource normal.").build());
		inventory.setItem(4, new ItemBuilder(Material.NETHERRACK).setName("§4§lNether").setLore("§aCliquez pour rejoindre le monde ressource nether.").build());
		inventory.setItem(6, new ItemBuilder(Material.END_STONE).setName("§5§lEnder").setLore("§aCliquez pour rejoindre le monde ressource ender.").build());

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