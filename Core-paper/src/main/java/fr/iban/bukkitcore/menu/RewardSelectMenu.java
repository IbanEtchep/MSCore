package fr.iban.bukkitcore.menu;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import fr.iban.bukkitcore.rewards.Reward;
import fr.iban.bukkitcore.rewards.RewardSelectCallback;
import fr.iban.bukkitcore.utils.Head;
import fr.iban.bukkitcore.utils.ItemBuilder;

public class RewardSelectMenu extends PaginatedMenu {

	private List<Reward> rewards;
	private Map<Integer, Reward> rewardAtSlot;
	private RewardSelectCallback callback;

	public RewardSelectMenu(Player player, List<Reward> rewards, RewardSelectCallback callback) {
		super(player);
		this.rewards = rewards;
		this.callback = callback;
	}

	@Override
	public String getMenuName() {
		return "§2Vos récompenses :";
	}

	@Override
	public int getRows() {
		return 6;
	}

	@Override
	public void handleMenu(InventoryClickEvent e) {
		Player player = (Player) e.getWhoClicked();
		ItemStack item = e.getCurrentItem();

		if(e.getClickedInventory() != e.getView().getTopInventory()) {
			return;
		}

		checkBottonsClick(item, player);


		Reward reward = rewardAtSlot.get(e.getSlot());

		if(reward == null) {
			return;
		}

		callback.select(reward);
	}

	@Override
	public int getElementAmount() {
		return rewards.size();
	}

	@Override
	public void setMenuItems() {
		addMenuBorder();

		this.rewardAtSlot = new HashMap<>();

		if(rewards != null && !rewards.isEmpty()) {
			for(int i = 0; i < getMaxItemsPerPage(); i++) {
				index = getMaxItemsPerPage() * page + i;
				if(index >= rewards.size()) break;
				if (rewards.get(index) != null){
					final int slot = inventory.firstEmpty();
					Reward reward = rewards.get(index);
					rewardAtSlot.put(slot, reward);
					inventory.setItem(slot, getRewardItem(reward));
				}
			}
		}	
	}

	private ItemStack getRewardItem(Reward reward) {
		return new ItemBuilder(Head.BAG.get())
				.setDisplayName("§2" + reward.name())
				.addLore("§aClic pour récupérer la récompense.")
				.build();
	}

}
