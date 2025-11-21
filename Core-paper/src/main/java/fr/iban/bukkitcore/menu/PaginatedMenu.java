package fr.iban.bukkitcore.menu;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.iban.bukkitcore.utils.Lang;

public abstract class PaginatedMenu extends Menu {

	protected int page = 0;
	protected int maxItemsPerPage = getSlots() - 14 - (getRows()*2);
	protected int index = 0;

	protected PaginatedMenu(Player player) {
		super(player);
	}
	
	public int getElementAmount() {
		return -1;
	}

	@Override
	public void addMenuBorder(){
		
		index = page*getMaxItemsPerPage();
		
		int lastRowFirst = (getRows()-1)*9;

		if(getElementAmount() != -1 && getElementAmount() > maxItemsPerPage && (index+getMaxItemsPerPage() + 1) <= getElementAmount()) {
			inventory.setItem(lastRowFirst+5, makeItem(Material.GREEN_STAINED_GLASS_PANE, Lang.get("menus.paginated.next")));
		}

		if(page > 0) {
			inventory.setItem(lastRowFirst+3, makeItem(Material.GREEN_STAINED_GLASS_PANE, Lang.get("menus.paginated.previous")));
		}

		inventory.setItem(lastRowFirst+4, makeItem(Material.RED_STAINED_GLASS_PANE, Lang.get("menus.paginated.close")));

		super.addMenuBorder();
	}

	protected void checkBottonsClick(ItemStack item, Player player) {
		checkNextBottonClick(item);
		checkPreviousBottonClick(item);
		checkCloseBottonClick(item, player);
	}

	protected void checkNextBottonClick(ItemStack item) {
		if(item.getItemMeta().getDisplayName().equalsIgnoreCase(Lang.get("menus.paginated.next"))){
			page += 1;
			super.open();
		}
	}

	protected void checkPreviousBottonClick(ItemStack item) {
		if(item.getItemMeta().getDisplayName().equalsIgnoreCase(Lang.get("menus.paginated.previous"))){
			page -= 1;
			if(page == 0) {
				index = 0;
			}
			super.open();
		}
	}

	protected void checkCloseBottonClick(ItemStack item, Player player) {
		if(item.getItemMeta().getDisplayName().equalsIgnoreCase(Lang.get("menus.paginated.close"))){
			player.closeInventory();
		}
	}
	
	public int getMaxItemsPerPage() {
		return maxItemsPerPage;
	}
}
