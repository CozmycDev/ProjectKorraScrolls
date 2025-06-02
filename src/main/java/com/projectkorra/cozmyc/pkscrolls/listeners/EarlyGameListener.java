package com.projectkorra.cozmyc.pkscrolls.listeners;

import com.projectkorra.cozmyc.pkscrolls.ProjectKorraScrolls;
import com.projectkorra.cozmyc.pkscrolls.models.Scroll;
import com.projectkorra.cozmyc.pkscrolls.utils.ColorUtils;
import com.projectkorra.cozmyc.pkscrolls.utils.ScrollItemFactory;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class EarlyGameListener implements Listener {

    private final ProjectKorraScrolls plugin;

    public EarlyGameListener(ProjectKorraScrolls plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onFirstLogBreak(BlockBreakEvent event) {
        if (!plugin.getConfigManager().getConfig().getBoolean("settings.earlyGameRewards.enabled", true)) {
            return;
        }

        Player player = event.getPlayer();

        if (!event.getBlock().getType().getKey().getKey().contains("log")) {
            return;
        }

        PersistentDataContainer data = player.getPersistentDataContainer();

        if (data.has(plugin.getNamespacedKey("first_log_break"), PersistentDataType.BYTE)) {
            ProjectKorraScrolls.getInstance().debugLog("Player " + player.getName() + " already received log break reward");
            return;
        }

        ProjectKorraScrolls.getInstance().debugLog("Giving log break reward to " + player.getName());
        data.set(plugin.getNamespacedKey("first_log_break"), PersistentDataType.BYTE, (byte) 1);
        giveReward(player);
    }

    @EventHandler
    public void onFirstCropPlant(PlayerInteractEvent event) {
        if (!plugin.getConfigManager().getConfig().getBoolean("settings.earlyGameRewards.enabled", true)) {
            return;
        }

        Player player = event.getPlayer();

        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (event.getClickedBlock() == null) {
            return;
        }
        if (event.getClickedBlock().getType() != Material.FARMLAND) {
            return;
        }
        if (event.getItem() == null || !isSeed(event.getItem().getType())) {
            return;
        }
        
        ProjectKorraScrolls.getInstance().debugLog("Player " + player.getName() + " planted a crop");
        
        PersistentDataContainer data = player.getPersistentDataContainer();

        if (data.has(plugin.getNamespacedKey("first_crop_plant"), PersistentDataType.BYTE)) {
            return;
        }
        
        ProjectKorraScrolls.getInstance().debugLog("Giving crop plant reward to " + player.getName());
        data.set(plugin.getNamespacedKey("first_crop_plant"), PersistentDataType.BYTE, (byte) 1);
        giveReward(player);
    }

    @EventHandler
    public void onFirstBedSleep(PlayerBedEnterEvent event) {
        Player player = event.getPlayer();

        if (!plugin.getConfigManager().getConfig().getBoolean("settings.earlyGameRewards.enabled", true)) {
            return;
        }

        PersistentDataContainer data = player.getPersistentDataContainer();

        if (data.has(plugin.getNamespacedKey("first_bed_sleep"), PersistentDataType.BYTE)) {
            return;
        }
        
        ProjectKorraScrolls.getInstance().debugLog("Giving bed sleep reward to " + player.getName());
        data.set(plugin.getNamespacedKey("first_bed_sleep"), PersistentDataType.BYTE, (byte) 1);
        giveReward(player);
    }

    private void giveReward(Player player) {
        ProjectKorraScrolls.getInstance().debugLog("Getting element-specific scrolls for " + player.getName());

        Scroll selectedScroll = plugin.getScrollManager().getRandomScrollForPlayer(player, false, false, false, true);
        if (selectedScroll == null) {
            ProjectKorraScrolls.getInstance().debugLog("Failed to get random scroll for player");
        }

        ProjectKorraScrolls.getInstance().debugLog("Giving scroll " + selectedScroll.getDisplayName() + " to " + player.getName());
        giveScrollsToPlayer(player, selectedScroll);
    }

    private void giveScrollsToPlayer(Player player, Scroll scroll) {
        int amount = plugin.getConfigManager().getConfig().getInt("settings.earlyGameRewards.amount", 3);
        ProjectKorraScrolls.getInstance().debugLog("Creating " + amount + " scrolls of " + scroll.getDisplayName() + " for " + player.getName());
        
        // Create and give the scrolls
        ItemStack scrollItem = ScrollItemFactory.createScroll(scroll);
        for (int i = 0; i < amount; i++) {
            player.getInventory().addItem(scrollItem);
        }

        // Send message to player
        player.sendMessage(ColorUtils.formatMessage(plugin.getConfigManager().getMessage("earlyGameReward")
            .replace("%amount%", String.valueOf(amount))
            .replace("%ability%", scroll.getDisplayName())));
    }

    private boolean isSeed(Material material) {
        return material == Material.WHEAT_SEEDS ||
               material == Material.BEETROOT_SEEDS ||
               material == Material.PUMPKIN_SEEDS ||
               material == Material.MELON_SEEDS ||
               material == Material.CARROT ||
               material == Material.SUGAR_CANE ||
               material == Material.COCOA_BEANS ||
               material == Material.POTATO;
    }
} 