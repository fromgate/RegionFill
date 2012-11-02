package fromgate.regionfill;

import com.google.common.base.Splitter;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Iterator;

public class RegionFill extends JavaPlugin implements Listener {

    private WorldGuardPlugin worldGuard;
    private boolean wgActive = false;

    @Override
    public void onEnable() {
        connectWorldGuard();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (!wgActive) {
            sender.sendMessage(ChatColor.RED + "World Guard not active");
            return true;
        }

        if (args.length == 3 && cmd.getName().equalsIgnoreCase("rgfill")) {
            World world = getWorldOrNotify(args[0], sender);
            if (world == null) return true;

            ProtectedRegion rg = getRegionOrNotify(args[1], world, sender);
            if (rg == null) return true;

            MaterialData fillMatData = getMatDataOrNotify(args[2], sender);
            if (fillMatData == null) return true;

            int minX = rg.getMinimumPoint().getBlockX();
            int minY = rg.getMinimumPoint().getBlockY();
            int minZ = rg.getMinimumPoint().getBlockZ();
            int maxX = rg.getMaximumPoint().getBlockX();
            int maxY = rg.getMaximumPoint().getBlockY();
            int maxZ = rg.getMaximumPoint().getBlockZ();

            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    for (int y = minY; y <= maxY; y++) {
                        world.getBlockAt(x, y, z).setTypeIdAndData(fillMatData.getItemTypeId(), fillMatData.getData(), false);
                    }
                }
            }
            return true;
        } else if (args.length == 4 && cmd.getName().equalsIgnoreCase("rgreplace")) {
            World world = getWorldOrNotify(args[0], sender);
            if (world == null) return true;

            ProtectedRegion rg = getRegionOrNotify(args[1], world, sender);
            if (rg == null) return true;

            MaterialData fromMatData = getMatDataOrNotify(args[2], sender);
            if (fromMatData == null) return true;

            MaterialData toMatData = getMatDataOrNotify(args[3], sender);
            if (toMatData == null) return true;
            if (toMatData.getData() == -1) {
                toMatData.setData((byte) 0);
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
                        Block block = world.getBlockAt(x, y, z);
                        if (block.getType() != fromMatData.getItemType()) continue;

                        if (fromMatData.getData() == -1 || block.getData() == fromMatData.getData()) {
                            block.setTypeIdAndData(toMatData.getItemTypeId(), toMatData.getData(), false);
                        }
                    }
                }
            }
            return true;
        }

        return false;
    }

    public World getWorldOrNotify(String worldName, CommandSender sender) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            sender.sendMessage(ChatColor.RED + "World " + worldName + " not loaded");
        }
        return world;
    }

    public ProtectedRegion getRegionOrNotify(String id, World world, CommandSender sender) {
        ProtectedRegion rg = worldGuard.getRegionManager(world).getRegion(id);
        if (rg == null) {
            sender.sendMessage(ChatColor.RED + "Region " + id + " not exists in the world " + world.getName());
        }
        return rg;
    }

    public MaterialData getMatDataOrNotify(String matDataString, CommandSender sender) {
        MaterialData matData = parseMatData(matDataString, ":");
        if (matData == null) {
            sender.sendMessage(ChatColor.RED + "Invalid id/data: " + ChatColor.DARK_PURPLE + matDataString + ChatColor.RED + ", format: <id>[:data]");
        }
        return matData;
    }

    public void connectWorldGuard() {
        Plugin worldGuardPlugin = getServer().getPluginManager().getPlugin("WorldGuard");
        if ((worldGuardPlugin != null) && (worldGuardPlugin instanceof WorldGuardPlugin)) {
            worldGuard = (WorldGuardPlugin) worldGuardPlugin;
            wgActive = true;
        }
    }

    /**
     *
     * @return data -1: undefined
     */
    public static MaterialData parseMatData(String idData, String delimiter) {
        if (idData == null || idData.isEmpty()) return null;
        idData = idData.trim();

        MaterialData result;
        try {
            if (idData.contains(delimiter)) {
                Iterator<String> itr = Splitter.on(delimiter).split(idData).iterator();
                result = new MaterialData(Integer.parseInt(itr.next()), Byte.parseByte(itr.next()));
            } else {
                result = new MaterialData(Integer.parseInt(idData), (byte) -1);
            }
        } catch (NumberFormatException ex) {
            return null;
        }
        if (result.getItemTypeId() < 0 || Material.getMaterial(result.getItemTypeId()) == null) {
            return null;
        } else if (result.getData() < -1) { // -1: undefined
            return null;
        }
        return result;
    }
}
