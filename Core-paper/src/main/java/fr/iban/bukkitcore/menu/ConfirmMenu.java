package fr.iban.bukkitcore.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import fr.iban.bukkitcore.menu.Menu;
import fr.iban.bukkitcore.utils.ConfirmCallback;
import fr.iban.bukkitcore.utils.ItemBuilder;

public class ConfirmMenu extends Menu {
	
	private final String title;
	private final String desc;
	private final ConfirmCallback callback;

	public ConfirmMenu(Player player, ConfirmCallback callback) {
		this(player, "§2Confirmer", "§aVoulez-vous vraiment faire cela?", callback);
	}
	
	public ConfirmMenu(Player player, String desc, ConfirmCallback callback) {
		this(player, "§2Confirmer", desc, callback);
	}
	
	public ConfirmMenu(Player player, String title, String desc, ConfirmCallback callback) {
		super(player);
		this.title = title;
		this.desc = desc;
		this.callback = callback;
	}

	@Override
	public String getMenuName() {
		return title;
	}

	@Override
	public int getRows() {
		return 3;
	}

	@Override
	public void handleMenu(InventoryClickEvent e) {
		ItemStack clicked = e.getCurrentItem();
		
		if(clicked.getType() == Material.GREEN_STAINED_GLASS_PANE) {
			callback.call(true);
		}else if(clicked.getType() == Material.RED_STAINED_GLASS_PANE) {
			callback.call(false);
		}
	}

	@Override
	public void setMenuItems() {
		ItemStack confirmItem = getConfirmItem();
		ItemStack cancelItem = getCancelItem();
		ItemStack middleItem = getMiddleItem();
		for (int i = 0; i < getSlots(); i++) {
			int rowSlot = (i+9) % 9;
			if(rowSlot < 4) {
				inventory.setItem(i, confirmItem);
			}else if(rowSlot > 4) {
				inventory.setItem(i, cancelItem);
			}else {
				inventory.setItem(i, middleItem);
			}
		}
	}
	
	private ItemStack getConfirmItem() {
		return new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE).setDisplayName("§2§lCONFIRMER").build();
	}
	
	private ItemStack getCancelItem() {
		return new ItemBuilder(Material.RED_STAINED_GLASS_PANE).setDisplayName("§4§lANNULER").build();
	}
	
	private ItemStack getMiddleItem() {
		return new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setDisplayName(desc).build();
	}
	

}
