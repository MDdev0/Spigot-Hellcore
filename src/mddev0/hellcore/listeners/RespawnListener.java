package mddev0.hellcore.listeners;

import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;

import mddev0.hellcore.Hellcore;
import mddev0.hellcore.Hellcore.Mode;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.matcher.NodeMatcher;
import net.luckperms.api.node.types.InheritanceNode;

public class RespawnListener implements Listener {
	
	private Hellcore plugin;
	private LuckPerms lp;
	
	public RespawnListener(Hellcore plugin, LuckPerms lp) {
		this.plugin = plugin;
		this.lp = lp;
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent respawn) {
		Player player = respawn.getPlayer();
		switch (plugin.mode()) {
		case CORRUPT:
			if (plugin.getConfig().getBoolean("autoChangeMode")) {
				try {
					// SCUFFED: I may or may not be getting data from files synchronously... shhhhh
					int count = countRegular().get() - 1;
					if (count == 0) {
						plugin.getServer().broadcastMessage(ChatColor.GOLD + "The last uncorrupted player has died. "
								+ ChatColor.RED + "Respawning is now disabled.");
						plugin.getLogger().info("The last regular player has died. Changing to permadeath mode.");
						plugin.mode(Mode.PERMADEATH);
						plugin.saveConfig();
						
					} else {
						plugin.getLogger().info("There are " + count + " regular players remaining.");
					}
				} catch (InterruptedException | ExecutionException e) {
					plugin.getLogger().log(Level.WARNING, "An error occurred while counting remaining players:", e);
				}
			}
		//$FALL-THROUGH$ (intended fall-through)
		case RESPAWN:
			setGroupAndTeam(player);
			// Set spawn location and send message
			respawn.setRespawnLocation(getRandomLocation(Bukkit.getWorld(plugin.getConfig().getString("respawnWorld"))));
			player.sendMessage(ChatColor.DARK_RED + plugin.getConfig().getString("respawnMessage"));
			if (plugin.getConfig().getBoolean("giveHelpCompass")) {
				ItemStack helpCompass = new ItemStack(Material.COMPASS);
				CompassMeta compassData = (CompassMeta) helpCompass.getItemMeta();
				compassData.setLodestoneTracked(false);
				compassData.setLodestone(plugin.getConfig().getLocation("exitLocation"));
				compassData.addEnchant(Enchantment.VANISHING_CURSE, 1, false);
				compassData.setDisplayName(ChatColor.DARK_GREEN + "Exit Compass");
				compassData.addItemFlags(ItemFlag.HIDE_ENCHANTS);
				helpCompass.setItemMeta(compassData);
				player.getInventory().addItem(helpCompass);
			}
			break;
		case PERMADEATH:
			player.setGameMode(GameMode.SPECTATOR);
			break;
		case DISABLE:
		default:
			//do nothing, default respawn logic
		}
	}

	private CompletableFuture<Integer> countRegular() {
		CompletableFuture<Integer> count = new CompletableFuture<>();
		NodeMatcher<InheritanceNode> match = NodeMatcher.key(InheritanceNode.builder(plugin.getConfig().getString("regularPermissionGroup")).build());
		lp.getUserManager().searchAll(match).thenAccept((Map<UUID, Collection<InheritanceNode>> map) -> {
			count.complete(map.keySet().size());
		});
		return count;
	}

	private void setGroupAndTeam(Player p) {
		// Reassign player group
		Group groupToSet = lp.getGroupManager().getGroup(plugin.getConfig().getString("escapingPermissionGroup"));
		lp.getUserManager().modifyUser(p.getUniqueId(), (User user) -> {
			Node nodeToSet = InheritanceNode.builder(groupToSet).build();
			user.data().add(nodeToSet); // Is now trying to escape
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
		try {
			// Reassign player team
			if (plugin.getConfig().getBoolean("useTeams"))
				plugin.getServer().getScoreboardManager().getMainScoreboard()
				.getTeam(plugin.getConfig().getString("escapingTeam"))
				.addEntry(p.getName());
		} catch (NullPointerException npx) {
			plugin.getLogger().log(Level.WARNING, "Could not assign " + p.getName() + " to "
					+ plugin.getConfig().getString("escapingTeam") + ", the team does not exist.");
		}
	}
	
	private Location getRandomLocation(World world) {
		int xMin = plugin.getConfig().getInt("xMin");
		int xMax = plugin.getConfig().getInt("xMax");
		int yMin = plugin.getConfig().getInt("yMin");
		int yMax = plugin.getConfig().getInt("yMax");
		int zMin = plugin.getConfig().getInt("zMin");
		int zMax = plugin.getConfig().getInt("zMax");
		boolean unsafe = true;
		Location loc;
		Random r = new Random();
		do {
			// Get a random location at the bottom of the Y range
			loc = new Location(world, (r.nextInt(Math.abs(xMin) + Math.abs(xMax)) + xMin), yMax, (r.nextInt(Math.abs(zMin) + Math.abs(zMax)) + zMin));
			for (int y = loc.getBlockY(); y > yMin; y--) {
				loc.setY(y);
				// Only safe if block below is solid
				if (!world.getBlockAt(loc.getBlockX(), loc.getBlockY()-1, loc.getBlockZ()).isPassable()) {
					// Only safe if block and 1 above are air
					if (loc.getBlock().isEmpty() && 
							world.getBlockAt(loc.getBlockX(), loc.getBlockY()+2, loc.getBlockZ()).isEmpty()) {
						unsafe = false;
						loc.setY(y+1);
						break; // Break out of the loop if safe
					} else
						unsafe = true;
				} else
					unsafe = true;
			}
		} while (unsafe);
		return loc;
	}
}
