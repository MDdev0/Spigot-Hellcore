package mddev0.hellcore.listeners;

import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import mddev0.hellcore.Hellcore;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;

public class NewPlayerListener implements Listener {

	Hellcore plugin;
	LuckPerms lp;

	public NewPlayerListener(Hellcore plugin, LuckPerms lp) {
		this.plugin = plugin;
		this.lp = lp;
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent join) {
		if (!plugin.getConfig().getBoolean("autoAssignNewPlayers")) return; // Cancel if not enabled
		if (!join.getPlayer().hasPlayedBefore()) {
			join.setJoinMessage(ChatColor.GOLD + join.getPlayer().getName() + " joined for the first time");
			setGroupAndTeam(join.getPlayer());
		}
	}

	private void setGroupAndTeam(Player p) {
		// Reassign player group
		Group groupToSet = lp.getGroupManager().getGroup(plugin.getConfig().getString("regularPermissionGroup"));
		lp.getUserManager().modifyUser(p.getUniqueId(), (User user) -> {
			Node nodeToSet = InheritanceNode.builder(groupToSet).build();
			user.data().add(nodeToSet); // Is now regular
			lp.getUserManager().saveUser(user);
		});
		try {
			// Reassign player team
			if (plugin.getConfig().getBoolean("useTeams"))
				plugin.getServer().getScoreboardManager().getMainScoreboard()
				.getTeam(plugin.getConfig().getString("regularTeam"))
				.addEntry(p.getName());
		} catch (NullPointerException npx) {
			plugin.getLogger().log(Level.WARNING, "Could not assign " + p.getName() + " to "
					+ plugin.getConfig().getString("regularTeam") + ", the team does not exist.");
		}
	}
}
