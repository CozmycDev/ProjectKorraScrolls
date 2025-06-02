package com.projectkorra.cozmyc.pkscrolls.listeners;

import com.projectkorra.cozmyc.pkscrolls.ProjectKorraScrolls;
import com.projectkorra.cozmyc.pkscrolls.models.Scroll;
import com.projectkorra.cozmyc.pkscrolls.utils.ScrollItemFactory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ScrollUpdateListener implements Listener {

    private final ProjectKorraScrolls plugin;

    public ScrollUpdateListener(ProjectKorraScrolls plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) {
            return;
        }

        int count = clickedItem.getAmount();

        if (!ScrollItemFactory.isScroll(clickedItem)) {
            return;
        }

        String abilityName = ScrollItemFactory.getScrollAbility(clickedItem);
        if (abilityName == null) {
            ProjectKorraScrolls.getInstance().debugLog("Could not get ability name from scroll");
            return;
        }

        Scroll scroll = plugin.getScrollManager().getScroll(abilityName);
        if (scroll == null) {
            ProjectKorraScrolls.getInstance().debugLog("Scroll not found for ability: " + abilityName);
            return;
        }

        ItemStack updatedScroll = ScrollItemFactory.createScroll(scroll);
        updatedScroll.setAmount(count);

        ProjectKorraScrolls.getInstance().debugLog("Updating scroll item for ability: " + abilityName);
        event.setCurrentItem(updatedScroll);
    }
} 