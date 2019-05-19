package net.minefs.TimedItems;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

public class LoreHider {
	public LoreHider(Plugin main) {
		ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(main, ListenerPriority.NORMAL,
				PacketType.Play.Server.SET_SLOT, PacketType.Play.Server.WINDOW_ITEMS) {
			@Override
			public void onPacketSending(PacketEvent event) {
				if (event.getPacketType() == PacketType.Play.Server.SET_SLOT) {
					PacketContainer packet = event.getPacket().deepClone();
					// int slot = packet.getIntegers().read(1);
					ItemStack item = packet.getItemModifier().read(0);
					ItemMeta itemMeta = item.getItemMeta();
					if (itemMeta != null && itemMeta.hasLore()) {
						List<String> lore = new ArrayList<String>(itemMeta.getLore());
						for (String s : itemMeta.getLore())
							if (s.equals(Main.staticline))
								lore.remove(s);
						itemMeta.setLore(lore);
						item.setItemMeta(itemMeta);
					}
					event.setPacket(packet);
				}
				if (event.getPacketType() == PacketType.Play.Server.WINDOW_ITEMS) {
					PacketContainer packet = event.getPacket().deepClone();
					for (ItemStack item : packet.getItemListModifier().read(0)) {
						ItemMeta itemMeta = item.getItemMeta();
						if (itemMeta != null && itemMeta.hasLore()) {
							List<String> lore = new ArrayList<String>(itemMeta.getLore());
							for (String s : itemMeta.getLore())
								if (s.equals(Main.staticline))
									lore.remove(s);
							itemMeta.setLore(lore);
							item.setItemMeta(itemMeta);
						}
					}
					event.setPacket(packet);
				}
			}
		});
	}
}
