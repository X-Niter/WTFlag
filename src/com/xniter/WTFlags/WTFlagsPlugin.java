package com.xniter.WTFlags;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class WTFlagsPlugin extends JavaPlugin {

    WorldGuardPlugin worldguard;
    Material _navigationWand;
    ProtocolManager protocolManager;
    StateFlag USE_DISPENSER;
    StateFlag SINGLE_USE_CHEST;

    @Override
    public void onEnable() {

        if (checkPlugin("ProtocolLib", false)) {
            protocolManager = ProtocolLibrary.getProtocolManager();
        }

        getServer().getPluginManager().registerEvents(new WTFlagsListener(this), this);

        // pull WorldEdit navigation wand information now
        WorldEdit worldEdit = WorldEdit.getInstance();
        String navigationWandMaterialName = worldEdit.getConfiguration().navigationWand;
        _navigationWand = Material.getMaterial(navigationWandMaterialName);

        Plugin wgPlugin = getServer().getPluginManager().getPlugin("WorldGuard");
        if (wgPlugin instanceof WorldGuardPlugin) {
            worldguard = (WorldGuardPlugin) wgPlugin;
        }

    }

    private <T extends Plugin> boolean checkPlugin(String name, boolean required) {
        Plugin plugin = getServer().getPluginManager().getPlugin(name);
        if (plugin == null) {
            if (required) {
                getLogger().warning("[" + getName() + "] " + name + " is required for this plugin to work; disabling.");
                getServer().getPluginManager().disablePlugin(this);
            }
            return false;
        }
        return true;
    }

    void expectTeleport(Player player) {
        long timestamp = player.getPlayerTime();
    }

    /**
     * Register Flags.
     */
    @Override
    public void onLoad() {
    	
    	saveDefaultConfig();

        WorldGuard worldGuard = WorldGuard.getInstance();
        FlagRegistry flagRegistry = worldGuard.getFlagRegistry();

        flagRegistry.register(new StringFlag("date"));
        flagRegistry.register(new StringFlag("created-by"));
        flagRegistry.register(new StringFlag("first-owner"));

        flagRegistry.register(USE_DISPENSER = new StateFlag("use-dispenser", getConfig().getBoolean("default-dispenser")));
        flagRegistry.register(SINGLE_USE_CHEST = new StateFlag("single-use-chest", getConfig().getBoolean("default-single-use-chest")));

        getLogger().log(Level.INFO, "Loaded all flags");
    }

}
