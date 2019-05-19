package net.minefs.TimedItems;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class CheckTask extends BukkitRunnable {

	private Functions f;

	public CheckTask(Functions f) {
		this.f = f;
	}

	@Override
	public void run() {
		for (Player p : new ArrayList<Player>(Bukkit.getOnlinePlayers())) {
			if (p.hasPermission("timeditems.ignore"))
				continue;
			f.checkInvAsync(p);
		}
	}

}
