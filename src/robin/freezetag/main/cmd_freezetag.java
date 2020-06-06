package robin.freezetag.main;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class cmd_freezetag implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			Player p = (Player) sender;
			if(args.length >= 1) {
				//COMMANDS
				
				//CREATE
				if(args[0].equalsIgnoreCase("create")) {
					if(p.isOp()) {
						if(args.length > 2) {
							Arena arena = new Arena(args[1], Integer.parseInt(args[2]), p.getLocation());
							arena.save();
							p.sendMessage(FreezeTag.prefix + "§aSucessfully created §2" + arena.getName() + " §aat your Location!");
						} else {
							sendHelp(p);
							return false;
						}
					} else {
						p.sendMessage(noPermission());
					}
				//JOIN
				} else if(args[0].equalsIgnoreCase("addRandomSpawn")) {
					if(p.isOp()) {
						if(args.length == 2) {
							for(Arena arena : FreezeTag.arenas) {
								if(arena.getName().equalsIgnoreCase(args[1])) {
									arena.addRandomSpawn(p.getLocation());
									arena.save();
									p.sendMessage(FreezeTag.prefix + "§aSucessfully added a random spawn for §2" + arena.getName() + " §aat your Location!");
									return true;
								}
							}
							p.sendMessage(FileManager.getMessage("messages.notExist").replaceAll("%arena%", args[1]));
							return false;
						}
					} else {
						p.sendMessage(noPermission());
					}
				} else if(args[0].equalsIgnoreCase("join")) {
					if(args.length >= 1) {
						for(Arena arena : FreezeTag.arenas) {
							if(arena.getName().equalsIgnoreCase(args[1])) {
								if(arena.getState() != Gamestate.INGAME) {
									if(arena.joinPlayer(p)) {
										p.sendMessage(FileManager.getMessage("messages.join").replaceAll("%arena%", arena.getName()));
										return true;
									} else {
										p.sendMessage(FileManager.getMessage("messages.full").replaceAll("%arena%", arena.getName()));
										return false;
									}
								} else {
									p.sendMessage(FileManager.getMessage("messages.ingame").replaceAll("%arena%", arena.getName()));
									return false;
								}
							}
						}
						p.sendMessage(FileManager.getMessage("messages.notExist").replaceAll("%arena%", args[1]));
					} else {
						sendHelp(p);
						return false;
					}
				//LIST
				} else if(args[0].equalsIgnoreCase("list")) {
					for(Arena arena : FreezeTag.arenas) {
						p.sendMessage("§c- " + arena.getName());
					}
				}
			} else {
				sendHelp(p);
			}
		}
		return false;
	}
	
	private void sendHelp(Player p) {
		p.sendMessage("§c/FreezeTag join <arena>");
		if(p.isOp()) {
			p.sendMessage("§c/FreezeTag create <name> <maxPlayers>");
			p.sendMessage("§c/FreezeTag addRandomSpawn <arena>");
		}
	}
	private String noPermission() {
		return "§cYou do not have permission to exectue this command!";
	}

}
