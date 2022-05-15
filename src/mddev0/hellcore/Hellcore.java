package mddev0.hellcore;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import mddev0.hellcore.listeners.ExitPortalMoveListener;
import mddev0.hellcore.listeners.IllegalPortalListener;
import mddev0.hellcore.listeners.RespawnListener;

import net.luckperms.api.LuckPerms;

public class Hellcore extends JavaPlugin {
	
	public enum Mode {
		DISABLE, RESPAWN, CORRUPT, PERMADEATH
	}
	
	private LuckPerms luckPerms;
	private static FileConfiguration config;
	private Mode currentMode;
	
	@Override
	public void onEnable() {
		// Set Up Configuration
		this.setupConfig();
		
		// Create LuckPerms API
		this.luckPerms = getServer().getServicesManager().load(LuckPerms.class);
		
		// Register Events
		getServer().getPluginManager().registerEvents(new RespawnListener(this, luckPerms), this);
		getServer().getPluginManager().registerEvents(new ExitPortalMoveListener(this, luckPerms), this);
		getServer().getPluginManager().registerEvents(new IllegalPortalListener(this, luckPerms), this);
	}
	
	@Override
	public void onDisable() {
	}
	
	private void setupConfig() {
		config = this.getConfig();
		config.addDefault("xMin", -1000);
		config.addDefault("xMax", 1000);
		config.addDefault("yMin", 0);
		config.addDefault("yMax", 125);
		config.addDefault("zMin", -1000);
		config.addDefault("zMax", 1000);
		config.addDefault("respawnWorld", "world_nether");
		config.addDefault("respawnMessage", "Welcome to the nether.");
		config.addDefault("escapeMessage", "You've escaped! Welcome back.");
		config.addDefault("corruptMessage", "You're back in the overworld, but it's not quite the same. You are now allowed to attack.");
		config.addDefault("portalExplodeMessage", "You really thought you could get out that easy?");
		config.addDefault("mode", Mode.RESPAWN.name());
		currentMode = Mode.valueOf(config.get("mode").toString().toUpperCase());
		config.addDefault("autoChangeMode", true);
		config.addDefault("regularPermissionGroup", "regular");
		config.addDefault("escapingPermissionGroup", "trapped");
		config.addDefault("corruptPermissionGroup", "corrupt");
		config.addDefault("useTeams", true);
		config.addDefault("regularTeam", "regular");
		config.addDefault("escapingTeam", "trapped");
		config.addDefault("corruptTeam", "corrupt");
		config.addDefault("exitLocation", new Location(getServer().getWorld("world_nether"),0.5,128,0.5));
		config.addDefault("exitRadius", 5.0);
		config.addDefault("exitToWorld", "world");
		config.addDefault("giveHelpCompass", true);
		config.options().copyDefaults(true);
    	saveConfig();
	}
	
	public Mode mode() { return currentMode; }
	
	public void mode(Mode m) {
		currentMode = m;
		config.set("mode", m.name());
		saveConfig();
	}
	
}
