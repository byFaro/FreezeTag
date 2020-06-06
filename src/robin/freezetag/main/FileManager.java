package robin.freezetag.main;

import java.io.File;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class FileManager {
	public static File file = new File ("plugins/" + FreezeTag.main.getName(), "config.yml");
	public static FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
	
	public static void setup() {
		cfg.addDefault("countdown.lobby", 60);
		cfg.addDefault("countdown.ingame", 300);
		cfg.addDefault("countdown.ending", 10);
		cfg.addDefault("groups.hunter", "&b&lHUNTER");
		cfg.addDefault("groups.fireman", "&6&lFIREMAN");
		cfg.addDefault("groups.runner", "&7&lRUNNER");
		cfg.addDefault("rewards.enabled", true);
		cfg.addDefault("rewards.command", "give %player% diamond 5");
		cfg.addDefault("messages.gameBegin", "The game begins! &a%player% &7is now the hunter!");
		cfg.addDefault("messages.countdown.begin", "The game begins in &a%time% &7seconds!");
		cfg.addDefault("messages.countdown.end", "The game ends in &a%time% &7seconds!");
		cfg.addDefault("messages.countdown.reset", "&cThe countdown has been resetted!");
		cfg.addDefault("messages.win", "THE &a%winners%s &7HAVE WON THE GAME!");
		cfg.addDefault("messages.endingMessage", "&aThanks for playing!");
		cfg.addDefault("messages.endingCause", "There is no %group% &7left!");

		cfg.addDefault("messages.changeRole", "You are now a %group%");
		cfg.addDefault("messages.onFreeze", "You were frozen &a%player% &7has joined the game!");
		cfg.addDefault("messages.onUnfreeze", "&a%player% &7has left the game!");
		cfg.addDefault("messages.onJoin", "&a%player% &7has joined the game!");
		cfg.addDefault("messages.onLeave", "&a%player% &7has left the game!");
		cfg.addDefault("messages.join", "&7You joined the arena &a%arena%&7!");
		cfg.addDefault("messages.ingame", "&cThe arena &a%arena% &c is already ingame!");
		cfg.addDefault("messages.full", "&cThe arena &a%arena% &c is full!");
		cfg.addDefault("messages.notExist", "&cThe arena &a%arena% &cdoes not exist");
		cfg.options().copyDefaults(true);
		saveCfg();
	}
	
	public static String getMessage(String path) {
		try {
			if(!path.startsWith("groups")) {
				return FreezeTag.prefix + ChatColor.translateAlternateColorCodes('&', cfg.getString(path));
			} return ChatColor.translateAlternateColorCodes('&', cfg.getString(path));
		} catch (Exception ex){
			return null;
		}
	}
	
	
	public static void saveArena(Arena arena) {
		try {
			cfg.set("arenas." 
				+ arena.getName() + ".spawn", 
				arena.getSpawn().getWorld().getName() + " " 
				+ arena.getSpawn().getX() + " " 
				+ arena.getSpawn().getY() 
				+ " " + arena.getSpawn().getZ());
			cfg.set("arenas." + arena.getName() + ".maxPlayers", arena.getMaxPlayers());
			cfg.set("arenas." + arena.getName() + ".randomSpawns", arena.getRandomSpawns());
			saveCfg();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	public static ArrayList<Arena> getArenas() {
		try {
			ArrayList<Arena> tmp = new ArrayList<>();
			for (String arenaName : cfg.getConfigurationSection("arenas").getKeys(false)) {
				System.out.println(FreezeTag.prefix + "Loading arena " + arenaName + "...");
				String locS[] = cfg.getString("arenas." + arenaName + ".spawn").split(" ");
				tmp.add(new Arena(
							arenaName, 
							cfg.getInt("arenas." + arenaName + ".maxPlayers"), 
							new Location(Bukkit.getWorld(locS[0]), Double.parseDouble(locS[1]), Double.parseDouble(locS[2]), Double.parseDouble(locS[3])),
							getRandomSpawns(arenaName)));
			}
			return tmp;
		} catch(Exception ex) {
//			ex.printStackTrace();
			System.out.println("[FreezeTag] There is no arena to load yet!");
			return null;
		}
	}
	private static ArrayList<Location> getRandomSpawns(String arenaName) {
		ArrayList<Location> randomSpawns = new ArrayList<>();
		try {
			for(Object loc : cfg.getList("arenas." + arenaName + ".randomSpawns")) {
				Location spawn = (Location) loc;
				randomSpawns.add(spawn);
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		return randomSpawns;
	}
	public static void saveCfg() {
		try {
			cfg.save(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
