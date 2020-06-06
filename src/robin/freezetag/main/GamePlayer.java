package robin.freezetag.main;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GamePlayer {
	
	private Player player;
	private Arena arena;
	private Role role;
	private boolean isFrozen;
	private HashMap<ItemStack, Integer> items;
	
	public GamePlayer(Player player, Arena arena) {
		this.player = player;
		this.arena = arena;
		this.role = Role.RUNNER;
		isFrozen = false;
		items = new HashMap<>();
		for(int i = 0; i < player.getInventory().getSize(); i++) {
			items.put(player.getInventory().getItem(i), i);
		}
	}
	
	public Player getPlayer() {
		return player;
	}
	public void setPlayer(Player player) {
		this.player = player;
	}
	public Arena getArena() {
		return arena;
	}
	public void setArena(Arena arena) {
		this.arena = arena;
	}
	public Role getRole() {
		return role;
	}
	public void setRole(Role role) {
		this.role = role;
		String title = Role.getGroupName(role);
		if(role == Role.HUNTER) {
			player.getInventory().setHelmet(new ItemStack(Material.ICE));
			player.getInventory().setChestplate(FreezeTag.hunterChestplate);
			title = "§b§lHUNTER";
			if(Bukkit.getVersion().contains("1.15")) 
			player.sendTitle(title, "§7try to catch all " + Role.getGroupName(Role.FIREMAN), 20, 20*2, 20);
		} else if(role == Role.FIREMAN) {
			if(Bukkit.getVersion().contains("1.15")) {
				player.getInventory().setHelmet(new ItemStack(Material.MAGMA_BLOCK));
				player.sendTitle(title, "§7you can unfreeze " + Role.getGroupName(Role.RUNNER), 20, 20*2, 20);
			} else {
				player.getInventory().setHelmet(new ItemStack(Material.matchMaterial("MAGMA")));
			}
			player.getInventory().setChestplate(FreezeTag.firemanChestplate);
			title = "§6§lFIREMAN";
		} else {
			player.getInventory().clear();
			player.getInventory().setChestplate(FreezeTag.runnerChestplate);
			title = "§7§lRUNNER";
			if(Bukkit.getVersion().contains("1.15")) 
			player.sendTitle(title, "§7escape the " + Role.getGroupName(Role.HUNTER), 20, 20*2, 20);
		}
		player.sendMessage(FileManager.getMessage("messages.changeRole").replaceAll("%group%", Role.getGroupName(role)));
	}

	public boolean isFrozen() {
		return isFrozen;
	}

	public void setFrozen(boolean isFrozen) {
		if(isFrozen) {
			player.getWorld().getBlockAt(player.getLocation()).setType(Material.ICE);
			arena.addIceBlocks(player.getLocation());
		} else {
			player.getWorld().getBlockAt(player.getLocation()).setType(Material.AIR);
//			arena.getIceBlocks().remove(player.getLocation());
		}
		this.isFrozen = isFrozen;
	}
	public void loadInventory() {
		player.getInventory().clear();
		for(ItemStack item : items.keySet()) {
			player.getInventory().setItem(items.get(item), item);
		}
	}
}
