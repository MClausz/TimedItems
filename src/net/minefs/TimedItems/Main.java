package net.minefs.TimedItems;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
	public static Plugin tm;
	public static boolean checkall = false;
	public static String format, prefix, staticline, expired, unactivated, activated, extender, extendstring = "";
	public static boolean allowextender, allowextends = true;
	public static long perm = 86400000000l;
	private Functions f;

	@Override
	public void onEnable() {
		f = new Functions(this);
		saveDefaultConfig();
		getServer().getPluginManager().registerEvents(new Listeners(f), this);
		tm = this;
		checkall = getConfig().getBoolean("check-all-lore");
		format = getConfig().getString("date-format").replace("&", "§");
		prefix = getConfig().getString("locale.prefix").replace("&", "§");
		staticline = getConfig().getString("locale.static-line").replace("&", "§");
		expired = getConfig().getString("locale.expired").replace("&", "§");
		activated = getConfig().getString("locale.activated").replace("&", "§");
		unactivated = getConfig().getString("locale.unactivated").replace("&", "§");
		extender = getConfig().getString("extender.name").replace("&", "§");
		allowextender = getConfig().getBoolean("allow-extender");
		allowextends = getConfig().getBoolean("allow-extends");
		extendstring = getConfig().getString("locale.extend-lock").replace("&", "§");
		perm = getConfig().getLong("extender.days-to-perm") * 86400000;
		getLogger().info("TimedItems v2.7 viet boi MasterClaus");
		getLogger().info("Donate cho tac gia qua email themasterclaus@gmail.com neu plugin co ich.");
		new CheckTask(f).runTaskTimerAsynchronously(tm, 600, 600);
	}

	@Override
	public void onDisable() {

	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length < 1) {

			sender.sendMessage(prefix + "§b§lCác câu lệnh của TimedItems:");
			sender.sendMessage("§a/timeditems add <số lượng> [đơn vị]: Thêm thời hạn cho item");
			sender.sendMessage("§a/timeditems set <ngày hết hạn>: Đặt ngày hết hạn cho item");
			sender.sendMessage("§a/timeditems del: Biến item thành vĩnh viễn");
			sender.sendMessage("§a/timeditems extender: Nhận phụ kiện gia hạn");
			sender.sendMessage("§a/timeditems lock: Khóa/mở khóa gia hạn");
			sender.sendMessage("§a/timeditems reload: Reload Config");
			sender.sendMessage("§aCác lệnh viết tắt: /th, /ti, /thoihan");
			return true;
		}
		if (args[0].equalsIgnoreCase("add")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(prefix + "Hay dung lenh nay trong game.");
				return true;
			}
			Player p = (Player) sender;
			if (args.length < 2) {
				p.sendMessage(prefix
						+ "§b§lDùng lệnh §e§l/timeditems add <số lượng> [đơn vị] §b§lđể thêm thời hạn cho item, vd: §e§l/timeditems add 7 ngay.");
				return true;
			}
			if (!args[1].matches("([0-9]+)")) {
				p.sendMessage(prefix + "§c§lGiá trị thời hạn phải là con số!");
				return true;
			}
			ItemStack i = p.getItemInHand();
			if (i == null || i.getType().equals(Material.AIR)) {
				p.sendMessage(prefix + "§c§lBạn cần cầm item trên tay để dùng lệnh này.");
				return true;
			}
			if (f.isActivated(i) || f.isUnactivated(i)) {
				p.sendMessage(prefix + "§c§lItem này đã có thời hạn sẵn.");
				return true;
			}
			ItemMeta im = i.getItemMeta();
			if (im == null) {
				p.sendMessage(prefix + "§c§lCó lỗi xảy ra khi thực hiện lệnh này.");
			}

			String type = null;
			if (args.length < 3)
				type = "ngày";
			else {
				if (f.getType(args[2]) != null)
					type = f.getType(args[2]);
				else {
					p.sendMessage(prefix + "§c§lĐơn vị thời lượng không hợp lệ (giay/phut/gio/ngay/tuan/thang/nam)");
					return true;
				}
			}

			List<String> lores;
			if (im.hasLore())
				lores = im.getLore();
			else
				lores = new ArrayList<String>();
			if (checkall)
				lores.add(staticline);
			lores.add(unactivated + args[1] + " " + type);
			im.setLore(lores);
			i.setItemMeta(im);
			p.sendMessage(prefix + "§a§lThêm thời hạn thành công: " + args[1] + " " + type + ".");
			return true;
		} else if (args[0].equalsIgnoreCase("del")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(prefix + "Hay dung lenh nay trong game.");
				return true;
			}
			Player p = (Player) sender;
			ItemStack i = p.getItemInHand();
			if (i == null || i.getType().equals(Material.AIR)) {
				p.sendMessage(prefix + "§c§lBạn cần cầm item trên tay để dùng lệnh này.");
				return true;
			}
			if (!f.isActivated(i) && !f.isUnactivated(i)) {
				p.sendMessage(prefix + "§c§lItem này chưa thiết lập thời hạn.");
				return true;
			}
			ItemMeta im = i.getItemMeta();
			List<String> lores = im.getLore();
			if (checkall) {
				lores.remove(lores.indexOf(staticline) + 1);
				lores.remove(lores.indexOf(staticline));
			} else
				lores.remove(lores.size() - 1);
			im.setLore(lores);
			i.setItemMeta(im);
			p.sendMessage(prefix + "§a§lXóa thời hạn thành công.");
		} else if (args[0].equalsIgnoreCase("set")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(prefix + "Hay dung lenh nay trong game.");
				return true;
			}
			Player p = (Player) sender;
			if (args.length < 2) {
				p.sendMessage(
						prefix + "§b§lDùng lệnh §e§l/timeditems set <ngày hết hạn> §b§lđể đặt ngày hết hạn cho item.");
				return true;
			}
			StringBuilder buffer = new StringBuilder();
			buffer.append(args[1]);
			for (int i = 2; i < args.length; i++) {
				buffer.append(' ').append(args[i]);
			}
			String date = buffer.toString();
			if (f.getTime(date) == 0) {
				p.sendMessage(prefix + "§c§lĐịnh dạng ngày hết hạn không hợp lệ!");
				p.sendMessage(prefix + "§c§lĐịnh dạng hợp lệ là §b§l" + format + "§c§l, vd: §b§l"
						+ f.getDate(new Timestamp(System.currentTimeMillis())));
				return true;
			}
			ItemStack i = p.getItemInHand();
			if (i == null || i.getType().equals(Material.AIR)) {
				p.sendMessage(prefix + "§c§lBạn cần cầm item trên tay để dùng lệnh này.");
				return true;
			}
			if (f.isActivated(i) || f.isUnactivated(i)) {
				p.sendMessage(prefix + "§c§lItem này đã có thời hạn sẵn.");
				return true;
			}
			ItemMeta im = i.getItemMeta();
			if (im == null) {
				p.sendMessage(prefix + "§c§lCó lỗi xảy ra khi thực hiện lệnh này.");
			}
			List<String> lores;
			if (im.hasLore())
				lores = im.getLore();
			else
				lores = new ArrayList<String>();
			if (checkall)
				lores.add(staticline);
			lores.add(activated + date);
			im.setLore(lores);
			i.setItemMeta(im);
			p.sendMessage(prefix + "§a§lĐặt ngày hết hạn thành công!");
			return true;
		} else if (args[0].equalsIgnoreCase("extender")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(prefix + "Hay dung lenh nay trong game.");
				return true;
			}
			Player p = (Player) sender;
			ItemStack e = new ItemStack(Material.valueOf(getConfig().getString("extender.material")));
			ItemMeta im = e.getItemMeta();
			im.setDisplayName(extender);
			List<String> lores = getConfig().getStringList("extender.lores");
			for (int n = 0; n < lores.size(); n++) {
				String lore = lores.get(n).replace("&", "§");
				lores.set(n, lore);
			}
			im.setLore(lores);
			e.setItemMeta(im);
			if (p.getInventory().firstEmpty() == -1) {
				p.sendMessage(prefix + "§c§lRương đồ của bạn đã đầy!");
				return true;
			}
			p.getInventory().addItem(e);
			p.sendMessage(prefix + "§a§lĐã nhận phụ kiện gia hạn.");
			return true;
		} else if (args[0].equalsIgnoreCase("lock")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(prefix + "Hay dung lenh nay trong game.");
				return true;
			}
			Player p = (Player) sender;
			ItemStack i = p.getItemInHand();
			if (i == null || i.getType().equals(Material.AIR)) {
				p.sendMessage(prefix + "§c§lBạn cần cầm item trên tay để dùng lệnh này.");
				return true;
			}
			ItemMeta im = i.getItemMeta();
			List<String> lores;
			if (im.hasLore())
				lores = im.getLore();
			else
				lores = new ArrayList<String>();
			if (lores.contains(extendstring)) {
				lores.remove(extendstring);
				p.sendMessage(prefix + "§a§lBỏ khóa giá hạn thành công.");
			} else {
				if (!checkall && (f.isUnactivated(i) || f.isActivated(i))) {
					String save = lores.get(lores.size() - 1);
					lores.set(lores.size() - 1, extendstring);
					lores.add(save);
				} else
					lores.add(extendstring);
				p.sendMessage(prefix + "§a§lThêm khóa gia hạn thành công.");
			}
			im.setLore(lores);
			i.setItemMeta(im);
		} else if (args[0].equalsIgnoreCase("reload")) {
			this.reloadConfig();
			checkall = getConfig().getBoolean("check-all-lore");
			format = getConfig().getString("date-format").replace("&", "§");
			prefix = getConfig().getString("locale.prefix").replace("&", "§");
			staticline = getConfig().getString("locale.static-line").replace("&", "§");
			expired = getConfig().getString("locale.expired").replace("&", "§");
			activated = getConfig().getString("locale.activated").replace("&", "§");
			unactivated = getConfig().getString("locale.unactivated").replace("&", "§");
			extender = getConfig().getString("extender.name").replace("&", "§");
			allowextender = getConfig().getBoolean("allow-extender");
			allowextends = getConfig().getBoolean("allow-extends");
			extendstring = getConfig().getString("locale.extend-lock").replace("&", "§");
			perm = getConfig().getLong("extender.days-to-perm") * 86400000;
			sender.sendMessage(prefix + "§a§lReload config thành công!");
		} else {
			sender.sendMessage(prefix + "§b§lCác câu lệnh của TimedItems:");
			sender.sendMessage("§a/timeditems add <số lượng> [đơn vị]: Thêm thời hạn cho item");
			sender.sendMessage("§a/timeditems set <ngày hết hạn>: Đặt ngày hết hạn cho item");
			sender.sendMessage("§a/timeditems del: Biến item thành vĩnh viễn");
			sender.sendMessage("§a/timeditems extender: Nhận phụ kiện gia hạn");
			sender.sendMessage("§a/timeditems lock: Khóa/mở khóa gia hạn");
			sender.sendMessage("§a/timeditems reload: Reload Config");
			sender.sendMessage("§aCác lệnh viết tắt: /th, /ti, /thoihan");
		}
		return true;
	}
}
