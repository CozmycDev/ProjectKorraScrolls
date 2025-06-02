package com.projectkorra.cozmyc.pkscrolls.listeners;

import com.projectkorra.cozmyc.pkscrolls.ProjectKorraScrolls;
import com.projectkorra.cozmyc.pkscrolls.models.Scroll;
import com.projectkorra.cozmyc.pkscrolls.utils.ColorUtils;
import com.projectkorra.cozmyc.pkscrolls.utils.ScrollItemFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class ScrollConsumeListener implements Listener {

    private final ProjectKorraScrolls plugin;

    public ScrollConsumeListener(ProjectKorraScrolls plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onScrollConsume(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (!ScrollItemFactory.isScroll(item)) {
            return;
        }

        event.setCancelled(true);
        ProjectKorraScrolls.getInstance().debugLog("Processing scroll consume for " + player.getName());

        String abilityName = ScrollItemFactory.getScrollAbility(item);
        if (abilityName == null) {
            ProjectKorraScrolls.getInstance().debugLog("Could not get ability name from scroll for " + player.getName());
            return;
        }

        Scroll scroll = plugin.getScrollManager().getScroll(abilityName);
        if (scroll == null) {
            ProjectKorraScrolls.getInstance().debugLog("Scroll not found for ability: " + abilityName);
            return;
        }

        if (ProjectKorraScrolls.getInstance().getPlayerDataManager().hasAbilityUnlocked(player, abilityName)) {
            ProjectKorraScrolls.getInstance().debugLog("Player " + player.getName() + " already has ability: " + abilityName);
            player.sendMessage(ColorUtils.formatMessage(plugin.getConfigManager().getMessage("alreadyUnlocked")));
            return;
        }

        ProjectKorraScrolls.getInstance().debugLog("Consuming scroll for " + player.getName() + ": " + abilityName);
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }

        boolean unlocked = plugin.getPlayerDataManager().consumeScroll(player, abilityName);
        
        if (unlocked) {
            ProjectKorraScrolls.getInstance().debugLog("Player " + player.getName() + " unlocked ability: " + abilityName);
            player.sendMessage(ColorUtils.formatMessage(
                plugin.getConfigManager().getMessage("abilityUnlocked")
                    .replace("%ability%", scroll.getDisplayName())
            ));
        } else {
            int progress = plugin.getPlayerDataManager().getProgress(player).getOrDefault(abilityName, 0);
            int required = scroll.getUnlockCount();
            ProjectKorraScrolls.getInstance().debugLog("Player " + player.getName() + " made progress on " + abilityName + ": " + progress + "/" + required);

            player.sendMessage(ColorUtils.formatMessage(
                plugin.getConfigManager().getMessage("scrollConsumed")
                    .replace("%ability%", scroll.getDisplayName())
                    .replace("%progress%", String.valueOf(progress))
                    .replace("%total%", String.valueOf(required))
            ));
        }
    }
}
