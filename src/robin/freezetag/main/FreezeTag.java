package robin.freezetag.main;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class FreezeTag extends JavaPlugin implements Listener {
	
	public static FreezeTag main;
	public static String prefix = "§bFreezeTag §8| §7";
	
	public static ArrayList<Arena> arenas = new ArrayList<>();

	public static ItemStack hunterChestplate;
	public static ItemStack firemanChestplate;
	public static ItemStack runnerChestplate;

	public static int countdownLobby;
	public static int countdownIngame;
	public static int countdownEnding;
	
	@Override
	public void onEnable() {
		main = this;
		this.getCommand("freezetag").setExecutor(new cmd_freezetag());
		Bukkit.getPluginManager().registerEvents(this, this);
		FileManager.setup();
		arenas = FileManager.getArenas();
		if(arenas != null) {
			System.out.println("Loading existing arenas...");
		}

		//############ ARMOR #############
		hunterChestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
		LeatherArmorMeta hChestM = (LeatherArmorMeta) hunterChestplate.getItemMeta();
		hChestM.setColor(Color.AQUA);
		hunterChestplate.setItemMeta(hChestM);

		firemanChestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
		LeatherArmorMeta fChestM = (LeatherArmorMeta) firemanChestplate.getItemMeta();
		fChestM.setColor(Color.ORANGE);
		firemanChestplate.setItemMeta(fChestM);
		
		runnerChestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
		LeatherArmorMeta rChestM = (LeatherArmorMeta) runnerChestplate.getItemMeta();
		rChestM.setColor(Color.GRAY);
		runnerChestplate.setItemMeta(rChestM);
		//################################
		
		try {
			countdownLobby = FileManager.cfg.getInt("countdown.lobby");
			countdownIngame = FileManager.cfg.getInt("countdown.ingame");
			countdownEnding = FileManager.cfg.getInt("countdown.ending");
		} catch(Exception ex) {
			countdownLobby = 60;
			countdownIngame = 300;
			countdownEnding = 10;
		}
		System.out.println("[FreezeTag] Countdown durations: Lobby (" + countdownLobby + ") | Ingame (" + countdownIngame + ") | Ending (" + countdownEnding + ")");
		System.out.println("[FreezeTag] Plugin successfully loaded!");		
	}
	
	/*
	 * LAST CHANGELOG
	 * 
	 * Added randomSpawn at ingame TP - /freezetag addrandomspawn <arena>
	 * Added customizable countdown durations
	 * Added Gamemode set to survival at joining Arena
	 * Added Save and load Inventory
	 * 
	 */
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onAnvil(PrepareAnvilEvent e) {
		for(ItemStack item : e.getInventory().getContents()) {
			if(item != null)
			System.out.println(item.getType());
		}
		System.out.println("Result: " + e.getResult().getType() + " ENCHANTS:");
		try {
			ItemStack result = e.getResult();
			ItemMeta resultMeta = result.getItemMeta();
			for(Enchantment en : resultMeta.getEnchants().keySet()) {
				System.out.println(en.getName());
			}
			for(String lore : resultMeta.getLore()) {
				System.out.println(lore);
			}
		} catch (Exception ex) {
			
		}
	}
	
	
	
	
	
	
	
	
	
	

	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		for(Arena arena : arenas) {
			if(arena.isPlayerPlaying(e.getPlayer())) {
				GamePlayer gp = arena.getGamePlayerByPlayer(e.getPlayer());
				if(gp.isFrozen() && gp.getRole() != Role.HUNTER) {
					if(!e.getFrom().equals(e.getTo())) {
						e.setCancelled(true);
					}
				}
			}
		}
	}
	
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		for(Arena arena : arenas) {
			if(arena.isPlayerPlaying(e.getPlayer())) {
				arena.leavePlayer(e.getPlayer());
			}
		}
	}
	@EventHandler
	public void onCatch(EntityDamageByEntityEvent e) {
		if(e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
			Player p = (Player) e.getEntity();
			Player d = (Player) e.getDamager();
			for(Arena arena : arenas) {
				if(arena.getState() == Gamestate.INGAME) {
					if(arena.isPlayerPlaying(p) && arena.isPlayerPlaying(d)) {
						GamePlayer gpp = arena.getGamePlayerByPlayer(p);
						GamePlayer gpd = arena.getGamePlayerByPlayer(d);
						if(gpp.isFrozen()) {
							if(gpd.getRole() == Role.FIREMAN) {
								gpp.setFrozen(false);
								gpp.getPlayer().sendMessage(FileManager.getMessage("messages.onUnfreeze").replaceAll("%player%", gpd.getPlayer().getDisplayName()));
								
							}
						} else {
							if(gpd.getRole() == Role.HUNTER) {
								gpp.setFrozen(true);
								gpp.getPlayer().sendMessage(FileManager.getMessage("messages.onFreeze").replaceAll("%player%", gpd.getPlayer().getDisplayName()));
								arena.checkEnd();
							}
						}
					}
				}
			}
		}
	}
}
