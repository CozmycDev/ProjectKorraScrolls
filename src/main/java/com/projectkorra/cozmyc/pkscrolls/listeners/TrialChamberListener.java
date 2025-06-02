package com.projectkorra.cozmyc.pkscrolls.listeners;

import com.projectkorra.cozmyc.pkscrolls.ProjectKorraScrolls;
import com.projectkorra.cozmyc.pkscrolls.models.Scroll;
import com.projectkorra.cozmyc.pkscrolls.utils.ColorUtils;
import com.projectkorra.cozmyc.pkscrolls.utils.ScrollItemFactory;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Vault;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Random;

@SuppressWarnings("UnstableApiUsage")
public class TrialChamberListener implements Listener {

    private final ProjectKorraScrolls plugin;
    private final Random random;
    private final NamespacedKey rewardedKey;

    public TrialChamberListener(ProjectKorraScrolls plugin) {
        this.plugin = plugin;
        this.random = new Random();
        this.rewardedKey = new NamespacedKey(plugin, "rewarded_players");
    }

    @EventHandler
    public void onVaultInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null || !(block.getState() instanceof Vault vault)) {
            return;
        }

        if (!plugin.getConfigManager().getConfig().getBoolean("settings.trialLoot.enabled", true)) {
            return;
        }

        Player player = event.getPlayer();

        ItemStack heldItem = event.getItem();

        ProjectKorraScrolls.getInstance().debugLog("Player " + player.getName() + " interacting with vault");
        ProjectKorraScrolls.getInstance().debugLog("Held item: " + (heldItem != null ? heldItem.toString() : "null"));
        ProjectKorraScrolls.getInstance().debugLog("Vault key: " + vault.getKeyItem());

        if (heldItem == null || !heldItem.isSimilar(vault.getKeyItem())) {
            ProjectKorraScrolls.getInstance().debugLog("Player does not have the correct key");
            return;
        }

        String rewardedPlayers = vault.getPersistentDataContainer().get(rewardedKey, PersistentDataType.STRING);
        if (rewardedPlayers != null && rewardedPlayers.contains(player.getUniqueId().toString())) {
            ProjectKorraScrolls.getInstance().debugLog("Player has already received rewards from this vault");
            return;
        }

        double chance = plugin.getConfigManager().getConfig().getDouble("settings.trialLoot.chance", 1.0);
        double roll = random.nextDouble();
        ProjectKorraScrolls.getInstance().debugLog("Trial loot chance roll: " + roll + " (needed: " + chance + ")");
        if (roll > chance) {
            ProjectKorraScrolls.getInstance().debugLog("Failed chance roll for trial loot");
            return;
        }

        int maxScrolls = plugin.getConfigManager().getConfig().getInt("settings.trialLoot.maxScrollsPerChest", 1);
        int numScrolls = random.nextInt(maxScrolls) + 1;
        ProjectKorraScrolls.getInstance().debugLog("Adding " + numScrolls + " scrolls to trial loot");

        for (int i = 0; i < numScrolls; i++) {
            ProjectKorraScrolls.getInstance().debugLog("Processing scroll " + (i + 1) + " of " + numScrolls);

            Scroll selectedScroll = plugin.getScrollManager().getRandomScrollForPlayer(player, false, true, false, false);
            if (selectedScroll == null) {
                ProjectKorraScrolls.getInstance().debugLog("Failed to get random scroll for player");
                continue;
            }

            // Drop the scroll independently of vault drops
            ItemStack scrollItem = ScrollItemFactory.createScroll(selectedScroll);

            vault.getLocation().getWorld().dropItem(vault.getLocation().add(0, 1, 0), scrollItem);
            ProjectKorraScrolls.getInstance().debugLog("Dropped scroll: " + selectedScroll.getDisplayName());

            String dropMessage = plugin.getConfigManager().getMessage("trialDrop");
            if (plugin.getConfigManager().getConfig().getBoolean("trialLoot.showDropMessage", true) 
                    && dropMessage != null && !dropMessage.trim().isEmpty()) {
                player.sendMessage(ColorUtils.formatMessage(
                    dropMessage
                        .replace("%scroll%", selectedScroll.getDisplayName())
                ));
            }
        }

        String newRewardedPlayers = rewardedPlayers == null ? 
            player.getUniqueId().toString() : 
            rewardedPlayers + "," + player.getUniqueId();
        vault.getPersistentDataContainer().set(rewardedKey, PersistentDataType.STRING, newRewardedPlayers);
        vault.update();
        ProjectKorraScrolls.getInstance().debugLog("Marked vault as rewarded for player " + player.getName());
    }
}
