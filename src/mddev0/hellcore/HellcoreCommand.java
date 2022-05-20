package mddev0.hellcore;

import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import mddev0.hellcore.Hellcore.Mode;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;

public class HellcoreCommand implements CommandExecutor {
	
	private Hellcore plugin;
	private LuckPerms lp;

	public HellcoreCommand(Hellcore plugin, LuckPerms lp) {
		this.plugin = plugin;
		this.lp = lp;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// Arguments Check
		if (args.length < 1) {
			sender.sendMessage(ChatColor.RED + "Usage: /hellcore <mode | setplayer | setexit | config | help>");
			return true;
		}
		
		switch (args[0].toLowerCase()) {
		// CHANGE MODE
		case "mode":
			if (args.length < 2) {
				sender.sendMessage(ChatColor.RED + "Usage: /hellcore mode <disable | respawn | corrupt | permadeath | query>");
				return true;
			}
			switch (args[1].toLowerCase()) {
			case "disable":
				plugin.mode(Mode.DISABLE);
				sender.sendMessage(ChatColor.GRAY + "The plugin has been disabled.");
				return true;
			case "respawn":
				plugin.mode(Mode.RESPAWN);
				sender.sendMessage(ChatColor.DARK_AQUA + "Players will now respawn and be able to escape normally.");
				return true;
			case "corrupt":
				plugin.mode(Mode.CORRUPT);
				sender.sendMessage(ChatColor.YELLOW + "Players will now be corrupted upon escape.");
				return true;
			case "permadeath":
				plugin.mode(Mode.PERMADEATH);
				sender.sendMessage(ChatColor.RED + "Players will no longer respawn.");
				return true;
			case "query":
				sender.sendMessage(ChatColor.GRAY + "The current mode is: " + plugin.mode().name().toLowerCase());
				return true;
			default:
				sender.sendMessage(ChatColor.RED + "Usage: /hellcore mode <disable | respawn | corrupt | permadeath | query>");
				return true;
			}
		// CHANGE PLAYER
		case "setplayer":
			if (args.length < 3) {
				sender.sendMessage(ChatColor.RED + "Usage: /hellcore setplayer <name> <regular | escaping | corrupt>");
				return true;
			}
			switch (args[2].toLowerCase()) {
			case "regular":
				try {
					setPlayer(plugin.getServer().getPlayer(args[1]), "regular");
					sender.sendMessage(ChatColor.GRAY + plugin.getServer().getPlayer(args[1]).getName() + " is now grouped as " + ChatColor.AQUA + "Regular" + ChatColor.GRAY + ".");
					return true;
				} catch (NullPointerException npx) {
					sender.sendMessage(ChatColor.RED + "The requested player is offline.");
					return true;
				}
			case "escaping":
				try {
					setPlayer(plugin.getServer().getPlayer(args[1]), "escaping");
					sender.sendMessage(ChatColor.GRAY + plugin.getServer().getPlayer(args[1]).getName() + " is now grouped as " + ChatColor.YELLOW + "Escaping" + ChatColor.GRAY + ".");
					return true;
				} catch (NullPointerException npx) {
					sender.sendMessage(ChatColor.RED + "The requested player is offline.");
					return true;
				}
			case "corrupt":
				try {
					setPlayer(plugin.getServer().getPlayer(args[1]), "corrupt");
					sender.sendMessage(ChatColor.GRAY + plugin.getServer().getPlayer(args[1]).getName() + " is now grouped as " + ChatColor.DARK_PURPLE + "Corrupt" + ChatColor.GRAY + ".");
					return true;
				} catch (NullPointerException npx) {
					sender.sendMessage(ChatColor.RED + "The requested player is offline.");
					return true;
				}
			default:
				sender.sendMessage(ChatColor.RED + "Usage: /hellcore setplayer <name> <regular | escaping | corrupt>");
				return true;
			}
		// CHANGE EXIT
		case "setexit":
			if (args.length < 2) {
				sender.sendMessage(ChatColor.RED + "Usage: /hellcore setexit <pos | radius>");
				return true;
			}
			if (args[1].toLowerCase().equals("pos")) {
				try {
					if (args.length < 3) {
						plugin.getConfig().set("exitLocation", plugin.getServer().getPlayer(sender.getName()).getLocation());
						sender.sendMessage(ChatColor.DARK_GREEN + "Set the location of the exit portal to your location.");
						plugin.saveConfig();
						return true;
					} else if (args.length == 5) {
						Location loc = new Location(plugin.getServer().getPlayer(sender.getName()).getWorld(),
								Double.parseDouble(args[2]), Double.parseDouble(args[3]), Double.parseDouble(args[4]), 0F, 0F);
						plugin.getConfig().set("exitLocation", loc);
						sender.sendMessage(ChatColor.DARK_GREEN + "Set the location of the exit portal to " + 
								ChatColor.AQUA + "(" + loc.getX() + ", " + loc.getY() + ", " + loc.getZ() + "; " + loc.getYaw() + ", " + loc.getPitch() + ")" + ChatColor.DARK_GREEN + ".");
						plugin.saveConfig();
						return true;
					} else if (args.length == 7) {
						Location loc = new Location(plugin.getServer().getPlayer(sender.getName()).getWorld(),
								Double.parseDouble(args[2]), Double.parseDouble(args[3]), Double.parseDouble(args[4]), Float.parseFloat(args[5]), Float.parseFloat(args[6]));
						plugin.getConfig().set("exitLocation", loc);
						sender.sendMessage(ChatColor.DARK_GREEN + "Set the location of the exit portal to " + 
								ChatColor.AQUA + "(" + loc.getX() + ", " + loc.getY() + ", " + loc.getZ() + "; " + loc.getYaw() + ", " + loc.getPitch() + ")" + ChatColor.DARK_GREEN + ".");
						plugin.saveConfig();
						return true;
					} else {
						sender.sendMessage(ChatColor.RED + "Usage: /hellcore setexit pos [<x> <y> <z> [<yaw> <pitch>]]");
						return true;
					}
				} catch (NumberFormatException nfx ) {
					sender.sendMessage(ChatColor.RED + "Usage: /hellcore setexit pos [<x> <y> <z> [<yaw> <pitch>]]");
					return true;
				}
			} else if (args[1].toLowerCase().equals("radius")) {
				if (args.length < 3) {
					sender.sendMessage(ChatColor.RED + "Usage: /hellcore setexit radius <radius>");
					return true;
				}
				try {
					double radius = Double.parseDouble(args[2]);
					plugin.getConfig().set("exitRadius", radius);
					plugin.saveConfig();
					return true;
				} catch (NumberFormatException nfx) {
					sender.sendMessage(ChatColor.RED + "Usage: /hellcore setexit radius <radius>");
					return true;
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Usage: /hellcore setexit <pos | radius>");
				return true;
			}
		// CHANGE CONFIG
		case "config":
			sender.sendMessage(ChatColor.DARK_RED + "Sorry, this function hasn't been implemented yet");
			return true;
			// TODO: Change config values
		// HELP
		case "help":
			sender.sendMessage(ChatColor.GOLD + "Help for " + ChatColor.WHITE + "/hellcore" + ChatColor.GOLD + ":" + ChatColor.RESET + "\n" +
					ChatColor.WHITE + "/hellcore <mode | setplayer | setexit | config | help>\n" + ChatColor.GOLD +
					"mode: " + ChatColor.RESET + "/hellcore mode <disable | respawn | corrupt | permadeath | query>\n" + ChatColor.GOLD +
					"setplayer: " + ChatColor.RESET + "/hellcore setplayer <name> <regular | escaping | corrupt>\n" + ChatColor.GOLD +
					"setexit: " + ChatColor.RESET + "/hellcore setexit <pos | radius>\n" + ChatColor.GREEN +
					"  pos: " + ChatColor.RESET + "/hellcore setexit pos [<x> <y> <z> [<yaw> <pitch>]]\n" + ChatColor.GREEN +
					"  radius: " + ChatColor.RESET + "/hellcore setexit radius <radius>\n" + ChatColor.GOLD +
					"config: " + ChatColor.YELLOW + "NOT YET IMPLEMENTED\n" + ChatColor.GOLD +
					"help: " + ChatColor.RESET + "/hellcore help");
			return true;
		default: 
			sender.sendMessage(ChatColor.RED + "Usage: /hellcore <mode | setplayer | setexit | config | help>");
			return true;
		}
	}

	private void setPlayer(Player p, String groupName) {
		// Remove from escaping group
		Group escapeRemove = lp.getGroupManager().getGroup(plugin.getConfig().getString("escapingPermissionGroup"));
		lp.getUserManager().modifyUser(p.getUniqueId(), (User user) -> {
			Node nodeToRemove = InheritanceNode.builder(escapeRemove).build();
			user.data().remove(nodeToRemove); // Is now trying to escape
			lp.getUserManager().saveUser(user);
		});
		// Remove them from regular group
		Group regularRemove = lp.getGroupManager().getGroup(plugin.getConfig().getString("regularPermissionGroup"));
		lp.getUserManager().modifyUser(p.getUniqueId(), (User user) -> {
			Node nodeToRemove = InheritanceNode.builder(regularRemove).build();
			user.data().remove(nodeToRemove); // Is no longer regular
			lp.getUserManager().saveUser(user);
		});
		// Remove them from corrupt group
		Group corruptRemove = lp.getGroupManager().getGroup(plugin.getConfig().getString("corruptPermissionGroup"));
		lp.getUserManager().modifyUser(p.getUniqueId(), (User user) -> {
			Node nodeToRemove = InheritanceNode.builder(corruptRemove).build();
			user.data().remove(nodeToRemove); // Is no longer corrupt
			lp.getUserManager().saveUser(user);
		});
		// Reassign player group
				Group groupToSet = lp.getGroupManager().getGroup(plugin.getConfig().getString(groupName + "PermissionGroup"));
				lp.getUserManager().modifyUser(p.getUniqueId(), (User user) -> {
					Node nodeToSet = InheritanceNode.builder(groupToSet).build();
					user.data().add(nodeToSet); // Group set
					lp.getUserManager().saveUser(user);
				});
		try {
			// Reassign player team
			if (plugin.getConfig().getBoolean("useTeams"))
				plugin.getServer().getScoreboardManager().getMainScoreboard()
				.getTeam(plugin.getConfig().getString(groupName + "Team"))
				.addEntry(p.getName());
		} catch (NullPointerException npx) {
			plugin.getLogger().log(Level.WARNING, "Could not assign " + p.getName() + " to "
					+ plugin.getConfig().getString("escapingTeam") + ", the team does not exist.");
		}
	}
}
