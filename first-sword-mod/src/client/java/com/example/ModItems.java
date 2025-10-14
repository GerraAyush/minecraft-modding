package client.java.com.example;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup; // Note: ItemGroup is deprecated in newer Minecraft versions in favor of Registries.ITEM_GROUP and ItemGroup.Builder
import net.minecraft.item.ItemGroups; // The actual creative inventory tabs
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents; // Fabric API for item group modification

import java.util.function.Function;

// Assuming FabricDocsReference is a custom class you have defined
// import your.package.name.FabricDocsReference; // Adjust this based on its actual location


public class ModItems {
    public static final String MOD_ID = "modid";

    // Item that we want in the game, created using static method register in the current class.
    public static final Item SUSPICIOUS_SUBSTANCE = register(
        "suspicious_substance",
        settings -> new SuspiciousSubstance(settings), // Use your SuspiciousSubstance constructor
        new Item.Settings()
    );
    
    public static void initialize() {
        // Dummy method that we can call from other Classes to statically initialize the current class.
        // Static Initialization helps evaluation of all static fields of a class.

		// To add our item to the ItemGroup, we need to use Fabric API's item group events - 
		// specifically ItemGroupEvents.modifyEntriesEvent

        // Get the event for modifying entries in the ingredients group.
        // And register an event handler that adds our suspicious item to the ingredients group.
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS)
        .register((itemGroup) -> itemGroup.add(ModItems.SUSPICIOUS_SUBSTANCE));
    }

	public static Item register(String name, Function<Item.Settings, Item> itemFactory, Item.Settings settings) {
		// Create the item key.
		RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(ModItems.MOD_ID, name));

		// Create the item instance.
		Item item = itemFactory.apply(settings.registryKey(itemKey));

		// Register the item.
		Registry.register(Registries.ITEM, itemKey, item);

		return item;
	}
}
