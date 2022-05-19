package mddev0.hellcore.listeners;

import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import mddev0.hellcore.Hellcore;
import mddev0.hellcore.Hellcore.Mode;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;

public class ExitPortalMoveListener implements Listener {
	
	private Hellcore plugin;
	private LuckPerms lp;

	public ExitPortalMoveListener(Hellcore plugin, LuckPerms lp) {
		this.plugin = plugin;
		this.lp = lp;
	}
	
	@EventHandler
	public void onRadiusEntry(PlayerMoveEvent move) {
		Mode mode = plugin.mode();
		if (mode != Mode.CORRUPT && mode != Mode.RESPAWN && mode != Mode.PERMADEATH) return; // Cancel if escaping isn't enabled
		// NOTE: Escaping is possible during permadeath if the player was already attempting to escape.
		Location target = plugin.getConfig().getLocation("exitLocation");
		if (move.getTo().getWorld() != target.getWorld()) return; // Cancel if in same world
		if (move.getTo().distanceSquared(move.getFrom()) == 0) return; // Cancel if hasn't moved
		double radius = plugin.getConfig().getDouble("exitRadius");
	if (target.distanceSquared(move.getFrom()) < (radius*radius) ) return; // Cancel if player was already inside region
		/* AT THIS POINT:
		 * - Player is in correct dimension
		 * - Player has moved
		 * - Player was not already in target radius
		 */
		if (target.distanceSquared(move.getTo()) < (radius*radius)) { // PLAYER MOVED INTO RADIUS
			Player p = move.getPlayer();
			Group escapingGroup = lp.getGroupManager().getGroup(plugin.getConfig().getString("escapingPermissionGroup"));
			// Change group and team of players that are escaping
			// Also only announce if escaping
			if (lp.getUserManager().getUser(p.getUniqueId()).getInheritedGroups(
					lp.getContextManager().getQueryOptions(p)).contains(escapingGroup)) {
				setGroupAndTeam(p, escapingGroup, mode);
				plugin.getServer().broadcastMessage(ChatColor.BLUE + 
						plugin.getConfig().getString("escapeServerMessage").replaceAll("%PLAYER%", move.getPlayer().getName()));
				if (mode == Mode.RESPAWN)
					p.sendMessage(ChatColor.DARK_AQUA + plugin.getConfig().getString("escapeMessage"));
				else
					p.sendMessage(ChatColor.LIGHT_PURPLE + plugin.getConfig().getString("corruptMessage"));
			}
			// teleport players regardless of group
			move.getPlayer().teleport(plugin.getServer().getWorld(plugin.getConfig().getString("exitToWorld")).getSpawnLocation()
					, TeleportCause.PLUGIN);
		}
	}

	private void setGroupAndTeam(Player p, Group escapingGroup, Mode mode) {
		// Remove them from escaping group
		lp.getUserManager().modifyUser(p.getUniqueId(), (User user) -> {
			Node escapingNode = InheritanceNode.builder(escapingGroup).build();
			user.data().remove(escapingNode); // Is no longer escaping
			lp.getUserManager().saveUser(user);
		});
		// Add player to correct group
		Group groupToSet = lp.getGroupManager().getGroup(plugin.getConfig().getString(
				mode == Mode.RESPAWN ? "regularPermissionGroup" : "corruptPermissionGroup"));
		lp.getUserManager().modifyUser(p.getUniqueId(), (User user) -> {
			Node nodeToSet = InheritanceNode.builder(groupToSet).build();
			user.data().add(nodeToSet); // Set new group
			lp.getUserManager().saveUser(user);
		});
		try {
			// Reassign player team
			if (plugin.getConfig().getBoolean("useTeams"))
				plugin.getServer().getScoreboardManager().getMainScoreboard()
				.getTeam(plugin.getConfig().getString(
							mode == Mode.RESPAWN ? "regularTeam" : "corruptTeam"))
				.addEntry(p.getName());
		} catch (NullPointerException npx) {
			plugin.getLogger().log(Level.WARNING, "Could not assign " + p.getName() + " to "
					+ plugin.getConfig().getString(
							mode == Mode.RESPAWN ? "regularTeam" : "corruptTeam") 
					+ ", the team does not exist.");
		}
	}
}
