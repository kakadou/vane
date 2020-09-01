package org.oddlama.vane.util;

import static org.oddlama.vane.util.BlockUtil.drop_naturally;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class PlayerUtil {
	public static void apply_elytra_boost(final Player player, double factor) {
		final var v = player.getLocation().getDirection();
		v.normalize();
		v.multiply(factor);

		// Set velocity, play sound
		player.setVelocity(player.getVelocity().add(v));
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.4f, 2.0f);
	}

	public static boolean till_block(final Player player, final Block block) {
		// Create block break event for block to till and check if it gets cancelled
		final var break_event = new BlockBreakEvent(block, player);
		Bukkit.getPluginManager().callEvent(break_event);
		if (break_event.isCancelled()) {
			return false;
		}

		// Till block
		block.setType(Material.FARMLAND);

		// Play sound, swing arm
		player.getWorld().playSound(player.getLocation(), Sound.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1.0f, 1.0f);
		player.swingMainHand();
		return true;
	}

	public static boolean seed_block(final Player player, final ItemStack used_item, final Block block, final Material plant_type, final Material seed_type) {
		// Create block place event for seed to place and check if it gets cancelled
		final var below = block.getRelative(BlockFace.DOWN);
		final var place_event = new BlockPlaceEvent(block, below.getState(), below, used_item, player, true, EquipmentSlot.HAND);
		Bukkit.getPluginManager().callEvent(place_event);
		if (place_event.isCancelled()) {
			return false;
		}

		// Remove one seed from inventory if not in creative mode
		if (player.getGameMode() != GameMode.CREATIVE) {
			final var seedstack = new ItemStack(seed_type, 1);
			if (!player.getInventory().containsAtLeast(seedstack, 1)) {
				return false;
			}

			player.getInventory().removeItem(seedstack);
		}

		// Set block seeded
		block.setType(plant_type);
		final var ageable = (Ageable)block.getBlockData();
		ageable.setAge(0);
		block.setBlockData(ageable);

		// Play sound, swing arm
		player.getWorld().playSound(player.getLocation(),
				seed_type == Material.NETHER_WART
				? Sound.ITEM_NETHER_WART_PLANT
				: Sound.ITEM_CROP_PLANT, SoundCategory.BLOCKS, 1.0f, 1.0f);
		player.swingMainHand();
		return true;
	}

	public static boolean harvest_plant(final Player player, final Block block) {
		ItemStack[] drops = null;
		switch (block.getType()) {
			default:
				return false;

			case WHEAT:       drops = new ItemStack[] {new ItemStack(Material.WHEAT,       1 + (int)(Math.random() * 2.5))}; break;
			case CARROTS:     drops = new ItemStack[] {new ItemStack(Material.CARROT,      1 + (int)(Math.random() * 2.5))}; break;
			case POTATOES:    drops = new ItemStack[] {new ItemStack(Material.POTATO,      1 + (int)(Math.random() * 2.5))}; break;
			case BEETROOTS:   drops = new ItemStack[] {new ItemStack(Material.BEETROOT,    1 + (int)(Math.random() * 2.5))}; break;
			case NETHER_WART: drops = new ItemStack[] {new ItemStack(Material.NETHER_WART, 1 + (int)(Math.random() * 2.5))}; break;
		}

		if (!(block.getBlockData() instanceof Ageable)) {
			return false;
		}

		// Only harvest fully grown plants
		var ageable = (Ageable)block.getBlockData();
		if (ageable.getAge() != ageable.getMaximumAge()) {
			return false;
		}

		// Create block break event for block to harvest and check if it gets cancelled
		final var break_event = new BlockBreakEvent(block, player);
		Bukkit.getPluginManager().callEvent(break_event);
		if (break_event.isCancelled()) {
			return false;
		}


		// Simply reset crop state and swing arm
		ageable.setAge(0);
		block.setBlockData(ageable);
		player.swingMainHand();

		// Drop items
		for (ItemStack drop : drops) {
			drop_naturally(block, drop);
		}

		return true;
	}
}

