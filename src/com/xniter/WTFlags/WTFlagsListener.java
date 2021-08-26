package com.xniter.WTFlags;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.association.RegionAssociable;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import com.sk89q.worldguard.session.SessionManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;

public class WTFlagsListener implements Listener {

    private WTFlagsPlugin plugin;

    WTFlagsListener(WTFlagsPlugin plugin) {
        this.plugin = plugin;
    }

    private static List<String> isChestInUse = new ArrayList<String>();


    // We check this at a NORMAL priority, so we'll intercept it beforehand.
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getItem() != null && event.getItem().getType() == plugin._navigationWand) {
            plugin.expectTeleport(event.getPlayer());
        }
        if (!event.isCancelled() && event.getClickedBlock() != null) {
            Location location = event.getClickedBlock().getLocation();
            Player player = event.getPlayer();

            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                switch (event.getClickedBlock().getType()) {
                    case DISPENSER:
                        setCancelled(event, !testBuild(player, location, plugin.USE_DISPENSER), true);
                        break;
                    case CHEST:
                        if (testBuild(player, location, plugin.SINGLE_USE_CHEST)){
                            if (isChestInUse.isEmpty()) {
                                isChestInUse.add(player.getName());
                                if (isChestInUse.size() > 1) {
                                    isChestInUse.subList(1, isChestInUse.size()).clear();
                                }
                            }
                            if (!isChestInUse.get(0).contains(event.getPlayer().getName()) || !isChestInUse.get(0).equals(event.getPlayer().getName())) {
                                if (isChestInUse.size() > 1) {
                                    isChestInUse.subList(1, isChestInUse.size()).clear();
                                }
                                singleUseChestCancelled(event, testBuild(player, location, plugin.SINGLE_USE_CHEST), true);
                            }
                        }
                        else
                            setCancelled(event, !testBuild(player, location, plugin.SINGLE_USE_CHEST), true);
                        break;
                    default:
                }
            }
        }
    }

    private boolean testBuild(Player player, Location location, StateFlag flag) {
        World world = location.getWorld();
        RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        com.sk89q.worldedit.util.Location wrappedLocation = BukkitAdapter.adapt(location);
        LocalPlayer localPlayer = plugin.worldguard.wrapPlayer(player);
        return hasBypass(player, world) || query.testBuild(wrappedLocation, localPlayer, flag);
    }

    private boolean hasBypass(Player player, World world) {
        com.sk89q.worldedit.world.World wrappedWorld = BukkitAdapter.adapt(world);
        LocalPlayer wrappedPlayer = plugin.worldguard.wrapPlayer(player);

        SessionManager sessionManager = WorldGuard.getInstance().getPlatform().getSessionManager();
        return sessionManager.hasBypass(wrappedPlayer, wrappedWorld);
    }

    private void cancelEvent(Cancellable e, boolean notifyPlayer) {
        setCancelled(e, true, notifyPlayer);
    }

    private void setCancelled(Cancellable e, boolean cancel, boolean notifyPlayer) {
        e.setCancelled(cancel);
        if (e.isCancelled() && notifyPlayer && e instanceof PlayerEvent) {
            PlayerEvent playerEvent = (PlayerEvent) e;
            Player player = playerEvent.getPlayer();
            player.sendMessage(ChatColor.DARK_RED + "You don't have permission to use that in this area.");
        }
    }

    private void singleUseChestCancelled(Cancellable e, boolean cancel, boolean notifyPlayer) {
        e.setCancelled(cancel);
        if (e.isCancelled() && notifyPlayer && e instanceof PlayerEvent) {
            PlayerEvent playerEvent = (PlayerEvent) e;
            Player player = playerEvent.getPlayer();
            player.sendMessage(ChatColor.DARK_RED + "Container currently in use!");
        }
    }

    private boolean testState(Entity entity, StateFlag flag) {
        return testState(entity.getLocation(), flag);
    }

    private boolean testState(Location location, StateFlag flag) {
        RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        com.sk89q.worldedit.util.Location wrappedLocation = BukkitAdapter.adapt(location);
        return query.testState(wrappedLocation, (RegionAssociable) null, flag);
    }
}
