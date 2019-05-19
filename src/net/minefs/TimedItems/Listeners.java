package net.minefs.TimedItems;

import java.sql.Timestamp;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class Listeners implements Listener {

	private Functions f;

	public Listeners(Functions f) {
		this.f = f;
	}

	@EventHandler
	public void playerLogin(PlayerJoinEvent e) {
		final Player p = e.getPlayer();
		Bukkit.getScheduler().runTaskAsynchronously(Main.tm, new Runnable() {
			@Override
			public void run() {
				f.checkInv(p);
			}
		});
	}

	@EventHandler
	public void invClick(InventoryClickEvent e) {
		ItemStack item = e.getCurrentItem();
		Player player = (Player) e.getWhoClicked();
		if (e.getClickedInventory() != null) {
			if (!player.hasPermission("timeditems.ignore")) {
				if (f.isExpired(item)) {
					ItemMeta im = item.getItemMeta();
					e.setCurrentItem(null);
					String name = ((im.hasDisplayName()) ? im.getDisplayName()
							: item.getData().getItemType().toString());
					String message = Main.expired.replace("%vp", name);
					player.sendMessage(Main.prefix + message);
				} else
					f.activeTimed(item);
			}
			ItemStack cur = e.getCursor();
			if (f.isActivated(item) && !f.isExpired(item) && f.isExtendable(item)) {
				if (Main.allowextends && f.isActivated(cur) && !f.isExpired(cur) && cur.getAmount() == 1
						&& item.getAmount() == 1 && cur.getType().equals(item.getType())
						&& ((!cur.getItemMeta().hasDisplayName() && !item.getItemMeta().hasDisplayName())
								|| (item.getItemMeta().hasDisplayName() && cur.getItemMeta().hasDisplayName() && cur
										.getItemMeta().getDisplayName().equals(item.getItemMeta().getDisplayName())))) {
					List<String> loresc = cur.getItemMeta().getLore();
					ItemMeta im = item.getItemMeta();
					List<String> lores = im.getLore();
					long time;
					long extend;
					String target;
					String targetc;
					int indextarget;
					if (Main.checkall) {
						indextarget = lores.indexOf(Main.staticline) + 1;
						target = lores.get(lores.indexOf(Main.staticline) + 1);
						targetc = loresc.get(loresc.indexOf(Main.staticline) + 1);
					} else {
						indextarget = lores.size() - 1;
						target = lores.get(lores.size() - 1);
						targetc = loresc.get(loresc.size() - 1);
					}
					time = f.getTime(target.replace(Main.activated, ""));
					extend = f.getTime(targetc.replace(Main.activated, "")) - System.currentTimeMillis();
					long total = (time + extend) - System.currentTimeMillis();
					if (total >= 86400000000L) {
						lores.remove(indextarget);
						if (Main.checkall)
							lores.remove(lores.indexOf(Main.staticline));
						player.sendMessage(Main.prefix + "§a§lGộp thời hạn thành công, item đã trở thành vĩnh viễn!");
					} else {
						lores.set(indextarget, Main.activated + f.getDate(new Timestamp(time + extend)));
						player.sendMessage(Main.prefix + "§a§lGộp thời hạn thành công!");
					}
					im.setLore(lores);
					item.setItemMeta(im);
					e.setCurrentItem(item);
					player.setItemOnCursor(null);
					e.setCancelled(true);
				} else if (Main.allowextender && cur != null && cur.hasItemMeta()) {
					ItemMeta imcur = cur.getItemMeta();
					if (imcur.hasDisplayName() && imcur.getDisplayName().equals(Main.extender) && item.getAmount() == 1
							&& cur.getType()
									.equals(Material.valueOf(Main.tm.getConfig().getString("extender.material")))) {
						long extend = (long) 86400000 * (long) cur.getAmount();
						ItemMeta im = item.getItemMeta();
						List<String> lores = im.getLore();
						String target;
						int indextarget;
						if (Main.checkall) {
							indextarget = lores.indexOf(Main.staticline) + 1;
							target = lores.get(lores.indexOf(Main.staticline) + 1);
						} else {
							indextarget = lores.size() - 1;
							target = lores.get(lores.size() - 1);
						}
						long time = f.getTime(target.replace(Main.activated, ""));
						long total = (time + extend) - System.currentTimeMillis();
						if (total >= Main.perm) {
							lores.remove(indextarget);
							if (Main.checkall)
								lores.remove(lores.indexOf(Main.staticline));
							player.sendMessage(
									Main.prefix + "§a§lGia hạn thành công, item của bạn đã trở thành vĩnh viễn!");
						} else {
							lores.set(indextarget, Main.activated + f.getDate(new Timestamp(time + extend)));
							player.sendMessage(
									Main.prefix + "§a§lGia hạn thành công, số ngày được cộng: " + cur.getAmount());
						}
						im.setLore(lores);
						item.setItemMeta(im);
						e.setCurrentItem(item);
						player.setItemOnCursor(null);
						e.setCancelled(true);
					}
				}
			}
		}
		if (e.getInventory() instanceof AnvilInventory) {
			if (e.getRawSlot() == 2) {
				AnvilInventory anvil = (AnvilInventory) e.getInventory();
				ItemStack i = anvil.getItem(1);
				if (f.isActivated(i) || f.isUnactivated(i))
					e.setCancelled(true);
			}
		}
	}
}
