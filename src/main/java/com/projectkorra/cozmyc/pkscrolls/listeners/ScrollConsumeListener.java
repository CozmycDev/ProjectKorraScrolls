package com.projectkorra.cozmyc.pkscrolls.listeners;

import com.projectkorra.cozmyc.pkscrolls.ProjectKorraScrolls;
import com.projectkorra.cozmyc.pkscrolls.models.Scroll;
import com.projectkorra.cozmyc.pkscrolls.utils.ColorUtils;
import com.projectkorra.cozmyc.pkscrolls.utils.ScrollItemFactory;
import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;
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
            String message = scroll.getAlreadyUnlockedMessage();
            if (message == null) {
                message = plugin.getConfigManager().getMessage("alreadyUnlocked");
            }
            player.sendMessage(ColorUtils.formatMessage(message, "ability", scroll.getDisplayName()));
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
            String message = scroll.getUnlockMessage();
            if (message == null) {
                message = plugin.getConfigManager().getMessage("abilityUnlocked");
            }
            player.sendMessage(ColorUtils.formatMessage(message, "ability", scroll.getDisplayName()));
            
            CoreAbility ability = CoreAbility.getAbility(scroll.getAbilityName());

            player.sendMessage(ColorUtils.addColor(plugin.getConfigManager().getMessage("commands.progress.header")));
            player.sendMessage(ability.getElement().getColor() + ability.getDescription());
            player.sendMessage(ColorUtils.addColor(plugin.getConfigManager().getMessage("commands.progress.header")));
            player.sendMessage(ColorUtils.addColor("&e" + ability.getInstructions()));
            player.sendMessage(ColorUtils.addColor(plugin.getConfigManager().getMessage("commands.progress.header")));
            
            BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
            int currentSlot = bPlayer.getCurrentSlot() + 1;
            if (bPlayer.getAbilities().get(currentSlot) == null) {
                bPlayer.bindAbility(scroll.getAbilityName(), currentSlot);
                String boundMessage = scroll.getAbilityBoundMessage();
                if (boundMessage == null) {
                    boundMessage = plugin.getConfigManager().getMessage("abilityBound");
                }
                player.sendMessage(ColorUtils.formatMessage(
                    boundMessage,
                    "ability", scroll.getDisplayName(),
                    "slot", String.valueOf(currentSlot)
                ));
            } else {
                String slotMessage = scroll.getSlotAlreadyBoundMessage();
                if (slotMessage == null) {
                    slotMessage = plugin.getConfigManager().getMessage("slotAlreadyBound");
                }
                player.sendMessage(ColorUtils.formatMessage(
                    slotMessage,
                    "ability", scroll.getDisplayName()
                ));
            }
        } else {
            int progress = plugin.getPlayerDataManager().getProgress(player).getOrDefault(abilityName, 0);
            int required = scroll.getUnlockCount();
            ProjectKorraScrolls.getInstance().debugLog("Player " + player.getName() + " made progress on " + abilityName + ": " + progress + "/" + required);

            String message = scroll.getConsumeMessage();
            if (message == null) {
                message = plugin.getConfigManager().getMessage("scrollConsumed");
            }
            player.sendMessage(ColorUtils.formatMessage(
                message,
                "ability", scroll.getDisplayName(),
                "progress", String.valueOf(progress),
                "total", String.valueOf(required)
            ));
        }
    }
}
