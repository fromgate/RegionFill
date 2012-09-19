package fromgate.regionfill;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class RegionFill extends JavaPlugin implements Listener {

    private WorldGuardPlugin worldGuard;
    private boolean wgActive = false;

    private static final String RG_BYPASS_PREFIX = "_fb_"; // force build

    @Override
    public void onEnable() {
        connectWorldGuard();
        if (wgActive) {
            getServer().getPluginManager().registerEvents(this, this);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        checkForceBuild(event.getBlock(), event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        checkForceBuild(event.getBlock(), event);
    }

    private void checkForceBuild(Block block, Cancellable event) {
        if (!event.isCancelled()) return;

        ApplicableRegionSet regions = worldGuard.getRegionManager(block.getWorld()).getApplicableRegions(block.getLocation());
        for (ProtectedRegion curRegion : regions) {
            if (curRegion.getId().startsWith(RG_BYPASS_PREFIX)) {
                event.setCancelled(false);
                return;
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (cmd.getName().equalsIgnoreCase("rgfill") && args.length == 3) {
            if (!wgActive) {
                sender.sendMessage(ChatColor.RED + "World Guard not active");
                return true;
            }

            int id;
            try {
                id = Integer.parseInt(args[2]);
                if (id < 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                sender.sendMessage(ChatColor.RED + "Id must be a positive number");
                return true;
            }

            World world = Bukkit.getWorld(args[0]);
            if (world == null) {
                sender.sendMessage(ChatColor.RED + "World " + args[0] + " not loaded");
                return true;
            }

            ProtectedRegion rg = worldGuard.getRegionManager(world).getRegion(args[1]);
            if (rg == null) {
                sender.sendMessage(ChatColor.RED + "Region " + args[1] + " not exists in the world " + world.getName());
                return true;
            }

            int minX = rg.getMinimumPoint().getBlockX();
            int minY = rg.getMinimumPoint().getBlockY();
            int minZ = rg.getMinimumPoint().getBlockZ();
            int maxX = rg.getMaximumPoint().getBlockX();
            int maxY = rg.getMaximumPoint().getBlockY();
            int maxZ = rg.getMaximumPoint().getBlockZ();

            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    for (int y = minY; y <= maxY; y++) {
                        world.getBlockAt(x, y, z).setTypeId(id);
                    }
                }
            }

            return true;
        }

        return false;
    }

    public void connectWorldGuard() {
        Plugin worldGuardPlugin = getServer().getPluginManager().getPlugin("WorldGuard");
        if ((worldGuardPlugin != null) && (worldGuardPlugin instanceof WorldGuardPlugin)) {
            worldGuard = (WorldGuardPlugin) worldGuardPlugin;
            wgActive = true;
        }
    }
}
