package mddev0.hellcore.listeners;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import mddev0.hellcore.Hellcore;
import mddev0.hellcore.Hellcore.Mode;

public class FinalDeathListener implements Listener {

	Hellcore plugin;

	public FinalDeathListener(Hellcore plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onFinalDeath(PlayerDeathEvent death) {
		if (plugin.mode() == Mode.PERMADEATH) {
			death.getEntity().getWorld().strikeLightningEffect(death.getEntity().getLocation());
			death.getEntity().sendTitle(ChatColor.DARK_RED + "Game Over", ChatColor.GOLD + "There are no more respawns.", 2, 178, 20);
		}
	}
}
