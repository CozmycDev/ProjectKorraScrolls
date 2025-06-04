package com.projectkorra.cozmyc.pkscrolls.listeners;

import com.projectkorra.cozmyc.pkscrolls.ProjectKorraScrolls;
import com.projectkorra.cozmyc.pkscrolls.models.Scroll;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.attribute.AttributeModification;
import com.projectkorra.projectkorra.attribute.AttributeModifier;
import com.projectkorra.projectkorra.event.AbilityRecalculateAttributeEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ScrollAttributeListener implements Listener {

    private final ProjectKorraScrolls plugin;
    private final NamespacedKey scrollModifierKey;

    public ScrollAttributeListener(ProjectKorraScrolls plugin) {
        this.plugin = plugin;
        this.scrollModifierKey = new NamespacedKey(plugin, "scroll_modifier");
    }

    @EventHandler
    public void onAttributeRecalculate(AbilityRecalculateAttributeEvent event) {
        if (!plugin.getConfigManager().getConfig().getBoolean("settings.abilityLevelling.enabled", true)) {
            return;
        }

        CoreAbility ability = event.getAbility();
        Player player = ability.getPlayer();
        if (player == null) {
            ProjectKorraScrolls.getInstance().debugLog("Attribute recalculation: No player found for ability");
            return;
        }

        Scroll scroll = plugin.getScrollManager().getScroll(ability.getName());
        if (scroll == null) {
            ProjectKorraScrolls.getInstance().debugLog("Attribute recalculation: No scroll found for ability: " + ability.getName());
            return;
        }

        int totalProgress = plugin.getPlayerDataManager().getProgress(player).getOrDefault(ability.getName(), 0);
        int unlockCount = scroll.getUnlockCount();
        ProjectKorraScrolls.getInstance().debugLog("Attribute recalculation for " + player.getName() +
                ": ability=" + ability.getName() +
                ", totalProgress=" + totalProgress +
                ", unlockCount=" + unlockCount);

        int postUnlockProgress = Math.max(0, totalProgress - unlockCount);
        if (postUnlockProgress <= 0) {
            ProjectKorraScrolls.getInstance().debugLog("Attribute recalculation: No post-unlock progress for " + ability.getName());
            return;
        }

        Object originalValue = event.getOriginalValue();
        if (originalValue == null) {
            ProjectKorraScrolls.getInstance().debugLog("Attribute recalculation: Original value is null for " + event.getAttribute());
            return;
        }

        Class<?> valueType = originalValue.getClass();
        ProjectKorraScrolls.getInstance().debugLog("Attribute recalculation: Current value for " + event.getAttribute() +
                " is " + originalValue + " (type: " + valueType.getSimpleName() + ")");

        if (!Number.class.isAssignableFrom(valueType)) {
            ProjectKorraScrolls.getInstance().debugLog("Attribute recalculation: Value is not a number for " + event.getAttribute());
            return;
        }

        try {
            Number scaledValue = scroll.calculateScaledAttribute(event.getAttribute(), (Number) originalValue, postUnlockProgress);
            ProjectKorraScrolls.getInstance().debugLog("Attribute recalculation: Scaled value for " + event.getAttribute() +
                    " is " + scaledValue + " (type: " + scaledValue.getClass().getSimpleName() + ")");

            if (!scaledValue.equals(originalValue)) {
                AttributeModification modification = AttributeModification.of(
                        AttributeModifier.SET,
                        scaledValue,
                        AttributeModification.PRIORITY_HIGH,
                        scrollModifierKey
                );

                event.addModification(modification);
                ProjectKorraScrolls.getInstance().debugLog("Attribute recalculation: SET " + event.getAttribute() +
                        " to " + scaledValue + " (was " + originalValue + ") with priority " +
                        AttributeModification.PRIORITY_HIGH);

                ProjectKorraScrolls.getInstance().debugLog("Current modifications for " + event.getAttribute() + ": " +
                        event.getModifications().size());

            } else {
                ProjectKorraScrolls.getInstance().debugLog("Attribute recalculation: No modification needed for " + event.getAttribute());
            }
        } catch (Exception e) {
            ProjectKorraScrolls.getInstance().debugLog("Error processing attribute modification for " + event.getAttribute());
            e.printStackTrace();
        }
    }
}
