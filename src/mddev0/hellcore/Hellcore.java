package mddev0.hellcore;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import mddev0.hellcore.listeners.RespawnListener;
import net.luckperms.api.LuckPerms;

public class Hellcore extends JavaPlugin {
	
	public enum Mode {
		DISABLE, RESPAWN, CORRUPT, PERMADEATH
	}
	
	private LuckPerms luckPerms;
	private static FileConfiguration config;
	
	@Override
	public void onEnable() {
		this.setupConfig();
		getServer().getPluginManager().registerEvents(new RespawnListener(this, luckPerms), this);
	}
	
	@Override
	public void onDisable() {
		
	}
	
	private void setupConfig() {
		config = this.getConfig();
		config.addDefault("xMin", -1000);
		config.addDefault("xMax", 1000);
		config.addDefault("yMin", 0);
		config.addDefault("yMax", 127);
		config.addDefault("zMin", -1000);
		config.addDefault("zMax", 1000);
		config.addDefault("respawnWorld", "world_nether");
		config.addDefault("respawnMessage", "Welcome to the nether.");
		config.addDefault("mode", Mode.RESPAWN);
		config.addDefault("autoChangeMode", true); // TODO: IMPLEMENT
		config.addDefault("escapingPermissionGroup", "trapped"); // TODO: IMPLEMENT
		config.addDefault("corruptPermissionGroup", "corrupt"); // TODO: IMPLEMENT
		config.addDefault("useTeams", true); // TODO: IMPLEMENT
		config.addDefault("regularTeam", "regular"); // TODO: IMPLEMENT
		config.addDefault("escapingTeam", "trapped"); // TODO: IMPLEMENT
		config.addDefault("corruptTeam", "corrupt"); // TODO: IMPLEMENT
		config.addDefault("exitLocation", new Location(getServer().getWorld("world_nether"),0.5,128,0.5)); // TODO: IMPLEMENT
		config.addDefault("exitRadius", 5); // TODO: IMPLEMENT
		config.options().copyDefaults(true);
    	saveConfig();
	}
}
