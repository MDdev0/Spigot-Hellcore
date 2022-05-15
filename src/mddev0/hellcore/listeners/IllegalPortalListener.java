package mddev0.hellcore.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.util.Vector;

import mddev0.hellcore.Hellcore;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;

public class IllegalPortalListener implements Listener {
	
	private Hellcore plugin;
	private LuckPerms lp;

	public IllegalPortalListener(Hellcore plugin, LuckPerms lp) {
		this.plugin = plugin;
		this.lp = lp;
	}
	
	@EventHandler
	public void onIllegalPortalEntry(EntityPortalEnterEvent portal) {
		Group escapingGroup = lp.getGroupManager().getGroup(plugin.getConfig().getString("escapingPermissionGroup"));
		if (portal.getEntityType() != EntityType.PLAYER) return;
		Player player = (Player) portal.getEntity();
		
		if (lp.getUserManager().getUser(player.getUniqueId())
				.getInheritedGroups(lp.getContextManager().getQueryOptions(player)).contains(escapingGroup)) {
			player.getWorld().createExplosion(portal.getLocation().add(new Vector(0.5,0.5,0.5)), 5F, true, true);
			player.sendMessage(ChatColor.GOLD + plugin.getConfig().getString("portalExplodeMessage"));
		}
	}
}
