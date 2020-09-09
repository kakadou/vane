package org.oddlama.vane.core.menu;

import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.oddlama.vane.core.menu.Menu.ClickResult;
import org.oddlama.vane.core.functional.Function5;
import org.oddlama.vane.core.functional.Function3;

public class MenuItem implements MenuWidget {
	private int slot;
	private Function5<Player, Menu, MenuItem, ClickType, InventoryAction, ClickResult> on_click;
	private ItemStack item;

	public MenuItem(int slot, final ItemStack item) { this(slot, item, null); }
	public MenuItem(int slot, final ItemStack item, final Function3<Player, Menu, MenuItem, ClickResult> on_click) {
		this(slot, item, (player, menu, self, type, action) -> {
			if (!Menu.is_normal_click(type, action)) {
				return ClickResult.INVALID_CLICK;
			}
			return on_click.apply(player, menu, self);
		});
	}
	public MenuItem(int slot, final ItemStack item, final Function5<Player, Menu, MenuItem, ClickType, InventoryAction, ClickResult> on_click) {
		this.slot = slot;
		this.on_click = on_click;
		this.item = item;
	}

	public int slot() { return slot; }
	public ItemStack item(final Menu menu) {
		return menu.inventory().getItem(slot);
	}

	public boolean update(final Menu menu) {
		final var cur = item();
		if (cur != item) {
			menu.inventory().setItem(slot(), item);
			return true;
		} else {
			return false;
		}
	}

	public ClickResult click(final Player player, final Menu menu, final ItemStack item, int slot, final ClickType type, final InventoryAction action) {
		if (this.slot != slot) {
			return ClickResult.IGNORE;
		}

		if (on_click != null) {
			return on_click.apply(player, menu, this, type, action);
		} else {
			return ClickResult.IGNORE;
		}
	}
}
