package net.cloudapp.mcminecraftwest.bukkit.mcwater;

import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Vector;

/**
 * Created with IntelliJ IDEA.
 * User: Jason
 * Date: 9/1/12
 * Time: 8:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class McWater extends JavaPlugin {


	public McWater() {
	}

    @Override
    public void onEnable() {
        getLogger().info("McWater enabled");

		PluginManager manager = getServer().getPluginManager();
		manager.registerEvents(new WaterFlowListener(this), this);

    }


	@Override
    public void onDisable() {
		getLogger().info("McWater disabled");
    }

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		return false;
	}
}
