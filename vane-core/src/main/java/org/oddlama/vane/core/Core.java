package org.oddlama.vane.core;

import java.io.File;
import java.util.Collections;
import static org.oddlama.vane.core.item.CustomItem.is_custom_item;
import java.util.SortedSet;
import org.bukkit.event.block.Action;
import java.util.TreeSet;
import static org.oddlama.vane.util.MaterialUtil.is_tillable;
import java.util.logging.Level;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.entity.Player;
import com.destroystokyo.paper.MaterialTags;

import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.event.entity.EntityTargetEvent;

import org.oddlama.vane.annotation.VaneModule;
import org.oddlama.vane.annotation.lang.LangMessage;
import org.oddlama.vane.annotation.lang.LangString;
import org.oddlama.vane.core.module.Module;
import org.bukkit.event.player.PlayerInteractEvent;
import org.oddlama.vane.util.Message;

@VaneModule(name = "core", bstats = 8637, config_version = 1, lang_version = 1, storage_version = 1)
public class Core extends Module<Core> {
	/** The base offset for any model data used by vane plugins. */
	// "vane" = 0x76616e65, but the value will be saved as float (json...), so only -2^24 - 2^24 can accurately be represented.
	// therefore we use 0x76616e as the base value.
	public static final int ITEM_DATA_BASE_OFFSET = 0x76616e;
	/** The amount of reserved model data id's per section (usually one section per plugin). */
	public static final int ITEM_DATA_SECTION_SIZE = 0x10000; // 0x10000 = 65k
	/** The amount of reserved model data id's per section (usually one section per plugin). */
	public static final int ITEM_VARIANT_SECTION_SIZE = (1 << 6); // 65k total → 1024 (items) * 64 (variants per item)

	/** Returns the item model data given the section and id */
	public static int model_data(int section, int item_id, int variant_id) {
		return ITEM_DATA_BASE_OFFSET + section * ITEM_DATA_SECTION_SIZE + item_id * ITEM_VARIANT_SECTION_SIZE + variant_id;
	}

	@LangString
	public String lang_command_not_a_player;
	@LangString
	public String lang_command_permission_denied;

	@LangMessage
	public Message lang_invalid_time_format;

	// Module registry
	private SortedSet<Module<?>> vane_modules = new TreeSet<>((a, b) -> a.get_name().compareTo(b.get_name()));
	public void register_module(Module<?> module) { vane_modules.add(module); }
	public void unregister_module(Module<?> module) { vane_modules.remove(module); }
	public SortedSet<Module<?>> get_modules() { return Collections.unmodifiableSortedSet(vane_modules); }

	// Vane global command catch-all permission
	public Permission permission_command_catchall = new Permission("vane.*.commands.*", "Allow access to all vane commands (ONLY FOR ADMINS!)", PermissionDefault.FALSE);

	public Core() {
		// Create global command catch-all permission
		register_permission(permission_command_catchall);

		// Components
		new org.oddlama.vane.core.commands.Vane(this);
		new CommandHider(this);
	}

	public boolean generate_resource_pack() {
		try {
			var pack = new ResourcePackGenerator();
			pack.set_description("Vane plugin resource pack");
			pack.set_icon_png(getResource("pack.png"));

			for (var m : vane_modules) {
				m.generate_resource_pack(pack);
			}

			pack.write(new File("vane-resource-pack.zip"));
		} catch (Exception e) {
			log.log(Level.SEVERE, "Error while generating resourcepack", e);
			return false;
		}
		return true;
	}

	// Prevent entity targeting by tempting when the reason is a custom item.
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void on_pathfind(final EntityTargetEvent event) {
		if (event.getReason() != EntityTargetEvent.TargetReason.TEMPT) {
			return;
		}

		if (!(event.getTarget() instanceof Player)) {
			return;
		}

		final var player = (Player)event.getTarget();
		if (is_custom_item(player.getInventory().getItemInMainHand())) {
			return;
		}
		if (is_custom_item(player.getInventory().getItemInOffHand())) {
			return;
		}

		// Cancel event as it was induced by a custom item
		event.setCancelled(true);
	}

	// Prevent custom hoe items from tilling blocks
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void on_player_hoe_right_click_block(final PlayerInteractEvent event) {
		if (!event.hasBlock() || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}

		// Only when clicking a tillable block
		if (!is_tillable(event.getClickedBlock().getType())) {
			return;
		}

		// Only when using a custom item that is a hoe
		final var player = event.getPlayer();
		final var item = player.getEquipment().getItem(event.getHand());
		if (is_custom_item(item) && MaterialTags.HOES.isTagged(item)) {
			event.setCancelled(true);
		}
	}
}
