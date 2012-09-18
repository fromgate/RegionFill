package fromgate.regionfill;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class RegionFill extends JavaPlugin {
	
	WorldGuardPlugin worldguard;
	boolean wg_active=false;
	
	@Override
	public void onEnable() {
		wg_active= connectWorldGuard();
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		if ((sender instanceof Player)&&(!((Player) sender).hasPermission("regionfill.execute"))) return true;
		if(cmd.getName().equalsIgnoreCase("rgfill")&&(args.length==3)){
			if (!args[2].matches("[0-9]*")) return true;
			int id = Integer.parseInt(args[2]);
			
			World w = Bukkit.getWorld(args[0]);
			if (w == null) return true;
			ProtectedRegion rg = worldguard.getRegionManager(w).getRegion(args[1]);
			if (rg == null) return true;
			int minx = rg.getMinimumPoint().getBlockX();
			int miny = rg.getMinimumPoint().getBlockY();
			int minz = rg.getMinimumPoint().getBlockZ();
			int maxx = rg.getMaximumPoint().getBlockX();
			int maxy = rg.getMaximumPoint().getBlockY();
			int maxz = rg.getMaximumPoint().getBlockZ();
			
			for (int x = minx; x<=maxx;x++)
				for (int z = minz; z<=maxz;z++)
					for (int y = miny; y<=maxy;y++){
						Block b = w.getBlockAt(x, y, z);
						b.setTypeId(id);
					}
		}
		return false;
	}
	
	
	
	public boolean connectWorldGuard(){
		Plugin worldGuard = getServer().getPluginManager().getPlugin("WorldGuard");
		if ((worldGuard != null)&&(worldGuard instanceof WorldGuardPlugin)) {
			worldguard = (WorldGuardPlugin)worldGuard;
			return true;
		}
		return false;
	}

}
