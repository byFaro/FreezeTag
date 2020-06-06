package robin.freezetag.main;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class Arena {
	private String name;
	private int maxPlayers;
	private Location spawn;
	private ArrayList<Location> randomSpawns = new ArrayList<>();
	private int countdown;
	private int countdownLobby;
	private int countdownIngame;
	private int countdownEnding;
	private Gamestate state;
	private GamePlayer hunter;
	private ArrayList<GamePlayer> players = new ArrayList<>();
	private ArrayList<Location> iceBlocks = new ArrayList<>();
	private Role winners;

	public Arena(String name, int maxPlayers, Location spawn) {
		this.name = name;
		this.maxPlayers = maxPlayers;
		this.spawn = spawn;
		countdownLobby = 0;
		countdownIngame = 0;
		state = Gamestate.LOBBY;
		FreezeTag.arenas.add(this);
	}
	public Arena(String name, int maxPlayers, Location spawn, ArrayList<Location> randomSpawns) {
		this.name = name;
		this.maxPlayers = maxPlayers;
		this.spawn = spawn;
		this.randomSpawns = randomSpawns;
		countdownLobby = 0;
		countdownIngame = 0;
		state = Gamestate.LOBBY;
		FreezeTag.arenas.add(this);
	}

	public void startLobbyCountdown() {
		countdown = FreezeTag.countdownLobby;
		countdownLobby = Bukkit.getScheduler().scheduleSyncRepeatingTask(FreezeTag.main, new Runnable() {
			@Override
			public void run() {	
				if(countdown == 60 || countdown == 30 || countdown == 20 || (countdown <= 10 && countdown >= 1)) {
					sendMessage(FileManager.getMessage("messages.countdown.begin").replaceAll("%time%", String.valueOf(countdown)));
				}
				if(countdown == 0) {
					tpAllRandom(randomSpawns);
					hunter = players.get(new Random().nextInt(players.size()-1));
					hunter.setRole(Role.HUNTER);
					GamePlayer fireman = getRandomGamePlayer(Role.RUNNER);
					if(fireman != null && fireman != hunter) {
						fireman.setRole(Role.FIREMAN);
					}
					fireman = getRandomGamePlayer(Role.RUNNER);
					if(fireman != null && fireman != hunter) {
						fireman.setRole(Role.FIREMAN);
					}
					sendMessage(FileManager.getMessage("messages.gameBegin").replaceAll("%player%", hunter.getPlayer().getName()));
					startIngameCountdown();
				}
				countdown--;
			}
		}, 0, 20);
	}
	public void startIngameCountdown() {
		Bukkit.getScheduler().cancelTask(countdownLobby);
		state = Gamestate.INGAME;
		countdown = FreezeTag.countdownIngame;
		countdownIngame = Bukkit.getScheduler().scheduleSyncRepeatingTask(FreezeTag.main, new Runnable() {
			@Override
			public void run() {	
				if(countdown == 300 || countdown == 120 || countdown == 60 || countdown == 30 || countdown == 20 || (countdown <= 10 && countdown >= 1)) {
					sendMessage(FileManager.getMessage("messages.countdown.end").replaceAll("%time%", String.valueOf(countdown)));
				}
				if(countdown == 0) {
					endGame();
				}
				countdown--;
			}
		}, 0, 20);
	}
	public void startEndingCountdown() {
		Bukkit.getScheduler().cancelTask(countdownIngame);
		countdown = FreezeTag.countdownEnding;
		sendMessage(FileManager.getMessage("messages.win").replaceAll("%winners%", Role.getGroupName(winners)));
		for(Location iceBlock : iceBlocks) {
			iceBlock.getBlock().setType(Material.AIR);
		}
		countdownEnding = Bukkit.getScheduler().scheduleSyncRepeatingTask(FreezeTag.main, new Runnable() {
			@Override
			public void run() {	
				if(countdown == 0) {
					tpAll(spawn);
					sendMessage(FileManager.getMessage("messages.endingMessage"));
					boolean rewards = FileManager.cfg.getBoolean("rewards.enabled");
					for(GamePlayer gp : players) {
						gp.getPlayer().getInventory().clear();
						gp.loadInventory();
						if(rewards) {
							Bukkit.dispatchCommand(Bukkit.getConsoleSender(), FileManager.cfg.getString("rewards.command").replaceAll("%player%", gp.getPlayer().getName()));
						}
					}
					players.clear();
					state = Gamestate.LOBBY;
					Bukkit.getScheduler().cancelTask(countdownEnding);
				}
				countdown--;
			}
		}, 0, 20);
	}
	public void sendMessage(String message) {
		for(GamePlayer gp : players) {
			gp.getPlayer().sendMessage(message);
		}
	}
	public void tpAll(Location loc) {
		for(GamePlayer p : players) {
			p.getPlayer().teleport(loc);
		}
	}
	public void tpAllRandom(ArrayList<Location> locs) {
		if(!randomSpawns.isEmpty()) {
			int i = 0;
			for(GamePlayer p : players) {
				if(i < randomSpawns.size()) {
					p.getPlayer().teleport(randomSpawns.get(i));
					i++;
				} else {
					i = 0;
				}
			}
		} else {
			tpAll(spawn);
		}
	}
	public void save() {
		FileManager.saveArena(this);
	}
	public boolean isPlayerPlaying(Player player) {
		for(GamePlayer gp : players) {
			if(gp.getPlayer().equals(player)) return true;
		}
		return false;
	}
	public void checkEnd() {
		if(players.size() < 2) {
			if(state == Gamestate.LOBBY) {
				sendMessage(FileManager.getMessage("messages.countdown.reset"));
				Bukkit.getScheduler().cancelTask(countdownLobby);
			} else {
				endGame();
			}
		}
		if(getPercentOfFrozenFiremen() >= 100) {
			winners = Role.HUNTER;
			sendMessage(FileManager.getMessage("messages.endingCause").replaceAll("%group%", Role.getGroupName(winners)));
			endGame();
		}
		if(getHunter() == null) {
			winners = Role.RUNNER;
			sendMessage(FileManager.getMessage("messages.endingCause").replaceAll("%group%", Role.getGroupName(winners)));
			endGame();
		}
	}
	public void endGame() {
		startEndingCountdown();
	}
	public boolean joinPlayer(Player player) {
		if(players.size() < maxPlayers) {
			addPlayer(player);
			player.teleport(spawn);
			player.setGameMode(GameMode.SURVIVAL);
			player.getInventory().clear();
			sendMessage(FileManager.getMessage("messages.onJoin").replaceAll("%player%", player.getDisplayName()));
			if(players.size() == 2) {
				startLobbyCountdown();
			}
			return true;
		}
		return false;
	}
	public boolean leavePlayer(Player player) {
		for(GamePlayer gp : players) {
			if(gp.getPlayer().equals(player)) {
				gp.loadInventory();
				players.remove(gp);
				sendMessage(FileManager.getMessage("messages.onLeave").replaceAll("%player%", player.getDisplayName()));
				checkEnd();
				return true;
			}
		}
		return false;
	}
	public ArrayList<GamePlayer> getFiremen() {
		ArrayList<GamePlayer> tmp = new ArrayList<>();
		for(GamePlayer gp : players) {
			if(gp.getRole().equals(Role.FIREMAN)) {
				tmp.add(gp);
			}
		}
		return tmp;
	}
	public GamePlayer getHunter() {
		return hunter;
	}
	public ArrayList<GamePlayer> getRunner() {
		ArrayList<GamePlayer> tmp = new ArrayList<>();
		for(GamePlayer gp : players) {
			if(gp.getRole().equals(Role.RUNNER)) {
				tmp.add(gp);
			}
		}
		return tmp;
	}
	private int getPercentOfFrozenFiremen() {
		int frozen = 0;
		ArrayList<GamePlayer> firemen = getFiremen();
		for(GamePlayer gp : firemen) {
			if(gp.isFrozen()) frozen++;
		}
		return (frozen/firemen.size())*100;
	}
	public String getName() {
		return name;
	}
	public GamePlayer getGamePlayerByPlayer(Player player) {
		for(GamePlayer gp : players) {
			if(gp.getPlayer().equals(player)) {
				return gp;
			}
		}
		return null;
	}

	private boolean isRoleManned(Role role) {
		for(GamePlayer gp : players) {
			if(gp.getRole().equals(role)) return true;
		}
		return false;
	}
	public GamePlayer getRandomGamePlayer(Role role) {
		GamePlayer gp = null;
		if(isRoleManned(role)) {
			if(role == Role.FIREMAN && !getFiremen().isEmpty()) {
				gp = getFiremen().get(new Random().nextInt(getFiremen().size()-1));
			} else if(role == Role.RUNNER && !getRunner().isEmpty()) {
				gp = getRunner().get(rndInt(0, getRunner().size()-1));
			}
		}
		return gp;
	}
	public static int rndInt(int min, int max) {
		Random r = new Random();
		int i = r.nextInt((max-min) + 1) + min;
		return i;
	}
	public void setName(String name) {
		this.name = name;
	}

	public int getMaxPlayers() {
		return maxPlayers;
	}

	public void setMaxPlayers(int maxPlayers) {
		this.maxPlayers = maxPlayers;
	}

	public ArrayList<GamePlayer> getGamePlayers() {
		return players;
	}

	public void addPlayer(Player player) {
		players.add(new GamePlayer(player, this));
	}

	public Location getSpawn() {
		return spawn;
	}

	public void setSpawn(Location spawn) {
		this.spawn = spawn;
	}

	public int getCountdown() {
		return countdown;
	}

	public void setCountdown(int countdown) {
		this.countdown = countdown;
	}

	public Gamestate getState() {
		return state;
	}

	public void setState(Gamestate state) {
		this.state = state;
	}

	public ArrayList<Location> getIceBlocks() {
		return iceBlocks;
	}

	public void addIceBlocks(Location loc) {
		this.iceBlocks.add(loc);
	}
	public void addRandomSpawn(Location loc) {
		if(loc != null) {
			randomSpawns.add(loc);
		}
	}
	public ArrayList<Location> getRandomSpawns() {
		return randomSpawns;
	}
}
