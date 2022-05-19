package mddev0.hellcore;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import mddev0.hellcore.Hellcore.Mode;

import net.luckperms.api.LuckPerms;

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
			// TODO: Display possible commands
		}
		
		switch (args[0].toLowerCase()) {
		case "mode":
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
				sender.sendMessage(ChatColor.RED + "/hellcore mode <disable|respawn|corrupt|permadeath|query>");
				return true;
			}
		case "setplayer":
			// TODO: Change player 
			break;
		case "setexit":
			if (args[1] == "pos") {
				// TODO: Change position based on supplied coords or executor position
			} else if (args[1] == "radius") {
				// TODO: Change radius
			} else {
				// TODO: Error message
			}
			break;
		case "config":
			// TODO: Change config values
			break;
		case "help":
			// TODO: Display help
			break;
		default: 
			// TODO: Display possible commands
		}
		
		return false; // FIXME: Delete this later
	}

}
