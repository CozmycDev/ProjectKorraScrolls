package com.projectkorra.cozmyc.pkscrolls.listeners;

import com.projectkorra.cozmyc.pkscrolls.ProjectKorraScrolls;
import com.projectkorra.cozmyc.pkscrolls.models.Scroll;
import com.projectkorra.cozmyc.pkscrolls.utils.ScrollItemFactory;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class LootGenerateListener implements Listener {

    private final ProjectKorraScrolls plugin;
    private final Random random;

    public LootGenerateListener(ProjectKorraScrolls plugin) {
        this.plugin = plugin;
        this.random = new Random();
    }

    @EventHandler
    public void onLootGenerate(LootGenerateEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (!plugin.getConfigManager().getConfig().getBoolean("settings.lootGeneration.enabled", true)) {
            ProjectKorraScrolls.getInstance().debugLog("Chest loot is disabled");
            return;
        }

        Block block = event.getLootContext().getLocation().getBlock();
        if (block.getType() != Material.CHEST) {
            ProjectKorraScrolls.getInstance().debugLog("Looted block is not a chest");
            return;
        }

        ProjectKorraScrolls.getInstance().debugLog("Chest loot generation detected at " + block.getLocation());

        double chance = plugin.getConfigManager().getConfig().getDouble("settings.lootGeneration.chance", 0.1);
        double roll = random.nextDouble();
        ProjectKorraScrolls.getInstance().debugLog("Chest loot chance roll: " + roll + " (needed: " + chance + ")");
        if (roll > chance) {
            ProjectKorraScrolls.getInstance().debugLog("Failed chance roll for chest loot");
            return;
        }

        int maxScrolls = plugin.getConfigManager().getConfig().getInt("settings.lootGeneration.maxScrollsPerChest", 1);
        int numScrolls = random.nextInt(maxScrolls) + 1;
        ProjectKorraScrolls.getInstance().debugLog("Adding " + numScrolls + " scrolls to chest loot");

        for (int i = 0; i < numScrolls; i++) {
            ProjectKorraScrolls.getInstance().debugLog("Processing scroll " + (i + 1) + " of " + numScrolls);

            Scroll selectedScroll = plugin.getScrollManager().getRandomScrollForPlayer(player, true, false, false, false);
            if (selectedScroll == null) {
                ProjectKorraScrolls.getInstance().debugLog("Failed to get random scroll for player");
                continue;
            }

            // Drop the scroll independently of vault drops
            ItemStack scrollItem = ScrollItemFactory.createScroll(selectedScroll);
            event.getLoot().add(scrollItem);
            ProjectKorraScrolls.getInstance().debugLog("Added scroll to chest: " + selectedScroll.getDisplayName());
        }
    }
}
