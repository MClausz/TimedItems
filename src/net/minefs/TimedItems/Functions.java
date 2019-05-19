package net.minefs.TimedItems;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class Functions {

	private Plugin plugin;
	boolean isNewVersion = false;

	public Functions(Plugin plugin) {
		this.plugin = plugin;
		String packageName = plugin.getServer().getClass().getPackage().getName();
		String version = packageName.substring(packageName.lastIndexOf('.') + 1);
		plugin.getLogger().info("Spigot " + version + " detected.");
		if (!version.startsWith("v1_8_")) {
			isNewVersion = true;
		}
	}

	public long getTime(String target) {
		SimpleDateFormat formatter2 = new SimpleDateFormat(Main.format);
		Date date2;
		try {
			date2 = (Date) formatter2.parse(target);
			long time = date2.getTime();
			return time;
		} catch (ParseException e) {
			return 0;
		}
	}

	public String getDate(Timestamp stamp) {
		SimpleDateFormat formatter = new SimpleDateFormat(Main.format);
		Date date = new Date(stamp.getTime());
		String result = formatter.format(date);
		return result;
	}

	public void activeTimed(ItemStack i) {
		if (!isUnactivated(i))
			return;
		List<String> lores = i.getItemMeta().getLore();
		String target;
		if (Main.checkall) {
			target = lores.get(lores.indexOf(Main.staticline) + 1);
		} else {
			target = lores.get(lores.size() - 1);
		}
		Pattern pattern = Pattern.compile(Main.unactivated + "([0-9]+) (giây|phút|giờ|ngày|tuần|tháng|năm)");
		Matcher matcher = pattern.matcher(target);
		if (matcher.find()) {
			String result = Main.activated + getDate(new Timestamp(System.currentTimeMillis()
					+ TimedTypes.getType(matcher.group(2)).getDurarion() * (Long.parseLong(matcher.group(1)))))
							.toString();
			if (Main.checkall)
				lores.set(lores.indexOf(Main.staticline) + 1, result);
			else
				lores.set(lores.size() - 1, result);
			ItemMeta im = i.getItemMeta();
			im.setLore(lores);
			i.setItemMeta(im);
		}
	}

	public boolean isExtendable(ItemStack i) {
		if (!isActivated(i))
			return true;
		List<String> lores = i.getItemMeta().getLore();
		if (lores.contains(Main.extendstring))
			return false;
		return true;
	}

	public boolean isExpired(ItemStack i) {
		if (!isActivated(i))
			return false;
		List<String> lores = i.getItemMeta().getLore();
		String target;
		if (Main.checkall) {
			target = lores.get(lores.indexOf(Main.staticline) + 1);
		} else {
			target = lores.get(lores.size() - 1);
		}
		Long time = getTime(target.replace(Main.activated, ""));
		if (time <= System.currentTimeMillis()) {
			return true;
		}
		return false;
	}

	public boolean isActivated(ItemStack i) {
		if (i == null)
			return false;
		if (!i.hasItemMeta())
			return false;
		ItemMeta im = i.getItemMeta();
		if (!im.hasLore())
			return false;
		List<String> lores = im.getLore();
		String target;
		if (Main.checkall) {
			if (!lores.contains(Main.staticline))
				return false;
			target = lores.get(lores.indexOf(Main.staticline) + 1);
		} else {
			target = lores.get(lores.size() - 1);
		}
		if (!target.startsWith(Main.activated))
			return false;
		if (getTime(target.replace(Main.activated, "")) != 0)
			return true;
		return false;
	}

	public boolean isUnactivated(ItemStack i) {
		if (i == null)
			return false;
		if (!i.hasItemMeta())
			return false;
		ItemMeta im = i.getItemMeta();
		if (!im.hasLore())
			return false;
		List<String> lores = im.getLore();
		String target;
		if (Main.checkall) {
			if (!lores.contains(Main.staticline))
				return false;
			target = lores.get(lores.indexOf(Main.staticline) + 1);
		} else {
			target = lores.get(lores.size() - 1);
		}
		Pattern pattern = Pattern.compile(Main.unactivated + "([0-9]+) (giây|phút|giờ|ngày|tuần|tháng|năm)");
		Matcher matcher = pattern.matcher(target);
		if (matcher.find()) {
			return true;
		}
		return false;
	}

	public void checkInv(Player p) {
		for (ItemStack i : p.getInventory().getContents()) {
			if (isExpired(i)) {
				ItemMeta im = i.getItemMeta();
				p.getInventory().remove(i);
				String name = ((im.hasDisplayName()) ? im.getDisplayName() : i.getData().getItemType().toString());
				String message = Main.expired.replace("%vp", name);
				p.sendMessage(Main.prefix + message);
			} else
				activeTimed(i);
		}
		ItemStack[] armors = p.getInventory().getArmorContents();
		int n = 0;
		for (ItemStack i : p.getInventory().getArmorContents()) {
			if (isExpired(i)) {
				ItemMeta im = i.getItemMeta();
				armors[n] = null;
				String name = ((im.hasDisplayName()) ? im.getDisplayName() : i.getData().getItemType().toString());
				String message = Main.expired.replace("%vp", name);
				p.sendMessage(Main.prefix + message);
			} else {
				activeTimed(i);
				armors[n] = i;
			}
			n++;
		}
		p.getInventory().setArmorContents(armors);
		if (isNewVersion) {
			ItemStack i = p.getInventory().getItemInOffHand();
			if (isExpired(i))
				p.getInventory().setItemInOffHand(null);
		}
	}

	public void checkInvAsync(final Player p) {
		boolean check = false;
		List<ItemStack> c = new ArrayList<ItemStack>(Arrays.asList(p.getInventory().getContents().clone()));
		List<ItemStack> c2 = new ArrayList<ItemStack>(Arrays.asList(p.getInventory().getArmorContents().clone()));
		c.addAll(c2);
		for (final ItemStack i : c) {
			if (isExpired(i) || isUnactivated(i)) {
				check = true;
				break;
			}
		}
		if (check)
			Bukkit.getScheduler().runTask(plugin, new Runnable() {
				@Override
				public void run() {
					checkInv(p);
				}
			});
	}

	public String getType(String type) {
		switch (type) {
		case "giay":
			return "giây";
		case "phut":
			return "phút";
		case "gio":
			return "giờ";
		case "ngay":
			return "ngày";
		case "tuan":
			return "tuần";
		case "thang":
			return "tháng";
		case "nam":
			return "năm";
		default:
			return null;
		}
	}

}
