package net.cloudapp.mcminecraftwest.bukkit.mcwater;

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
public class McWater extends JavaPlugin implements Listener {

	public McWater() {
	}
    @Override
    public void onEnable() {
        getLogger().info("McWater enabled");

		PluginManager manager = getServer().getPluginManager();
		manager.registerEvents(this, this);

    }

	@EventHandler
	public void onBlockFromTo(BlockFromToEvent event) {
		event.setCancelled(true);
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
