package org.oddlama.vane.trifles.items;

import static org.oddlama.vane.util.BlockUtil.relative;
import static org.oddlama.vane.util.ItemUtil.MODIFIER_UUID_GENERIC_ATTACK_DAMAGE;
import static org.oddlama.vane.util.ItemUtil.MODIFIER_UUID_GENERIC_ATTACK_SPEED;
import static org.oddlama.vane.util.ItemUtil.damage_item;
import static org.oddlama.vane.util.MaterialUtil.is_seeded_plant;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.oddlama.vane.trifles.event.PlayerTeleportScrollEvent;
import static org.oddlama.vane.util.PlayerUtil.harvest_plant;
import static org.oddlama.vane.util.PlayerUtil.swing_arm;

import org.bukkit.inventory.RecipeChoice.MaterialChoice;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ShapelessRecipe;

import org.oddlama.vane.annotation.config.ConfigDouble;
import org.oddlama.vane.annotation.item.VaneItem;
import org.oddlama.vane.core.item.CustomItem;
import org.oddlama.vane.core.item.CustomItemVariant;
import org.oddlama.vane.core.module.Context;
import org.oddlama.vane.trifles.Trifles;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice.MaterialChoice;
import org.bukkit.inventory.ShapedRecipe;

import org.oddlama.vane.annotation.config.ConfigDouble;
import org.oddlama.vane.annotation.config.ConfigInt;
import org.oddlama.vane.annotation.item.VaneItem;
import org.oddlama.vane.core.item.CustomItem;
import org.oddlama.vane.core.item.CustomItemVariant;
import org.oddlama.vane.core.item.ItemVariantEnum;
import org.oddlama.vane.core.module.Context;
import org.oddlama.vane.trifles.Trifles;
import org.oddlama.vane.util.BlockUtil;

@VaneItem(name = "home_scroll")
public class HomeScroll extends CustomItem<Trifles, HomeScroll> {
	public static class HomeScrollVariant extends CustomItemVariant<Trifles, HomeScroll, SingleVariant> {
		public HomeScrollVariant(HomeScroll parent, SingleVariant variant) {
			super(parent, variant);
		}

		@Override
		public void register_recipes() {
			final var item = item();
			final var recipe = new ShapedRecipe(recipe_key(), item())
				.shape("pip",
				       "cbe",
				       "plp")
				.setIngredient('b', Material.ENCHANTED_BOOK)
				.setIngredient('p', Material.MAP)
				.setIngredient('i', new MaterialChoice(Tag.BEDS))
				.setIngredient('c', Material.COMPASS)
				.setIngredient('e', Material.ENDER_PEARL)
				.setIngredient('l', Material.CLOCK);

			add_recipe(recipe);
		}

		@Override
		public Material base() {
			return Material.CARROT_ON_A_STICK;
		}
	}

	public HomeScroll(Context<Trifles> context) {
		super(context, HomeScrollVariant::new);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false) // ignoreCancelled = false to catch right-click-air events
	public void on_player_right_click(final PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) {
			return;
		}

		// Get item variant
		final var player = event.getPlayer();
		final var item = player.getEquipment().getItem(event.getHand());
		final var variant = this.<HomeScrollVariant>variant_of(item);
		if (variant == null || !variant.enabled()) {
			return;
		}

		// Never actually use the base item if it's custom!
		event.setUseItemInHand(Event.Result.DENY);

		switch (event.getAction()) {
			default: return;
			case RIGHT_CLICK_AIR: break;
			case RIGHT_CLICK_BLOCK:
				// Require non-cancelled state (so it won't trigger for block-actions like chests)
				// Second check prevent original item usage (collecting liquids)
				if (event.useInteractedBlock() != Event.Result.DENY) {
					return;
				}
				break;
		}

		final var to_location = player.getBedSpawnLocation();
		if (to_location == null) {
			return;
		}

		final var current_location = player.getLocation();
		if (get_module().teleport_from_scroll(player, current_location, to_location)) {
			// Damage item
			damage_item(player, item, 1);
			swing_arm(player, event.getHand());
		}
	}
}