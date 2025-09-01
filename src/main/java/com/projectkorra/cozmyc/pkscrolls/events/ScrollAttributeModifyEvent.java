package com.projectkorra.cozmyc.pkscrolls.events;

import com.projectkorra.cozmyc.pkscrolls.models.Scroll;
import com.projectkorra.projectkorra.ability.CoreAbility;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event called when a scroll modifies an ability's attributes
 */
public class ScrollAttributeModifyEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Scroll scroll;
    private final CoreAbility ability;
    private final String attributeName;
    private final double originalValue;
    private final double newValue;
    private boolean cancelled;

    public ScrollAttributeModifyEvent(Player player, Scroll scroll, CoreAbility ability, 
                                    String attributeName, double originalValue, double newValue) {
        this.player = player;
        this.scroll = scroll;
        this.ability = ability;
        this.attributeName = attributeName;
        this.originalValue = originalValue;
        this.newValue = newValue;
        this.cancelled = false;
    }

    /**
     * Gets the player whose ability is being modified
     * @return The player whose ability is being modified
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the scroll that is modifying the ability
     * @return The scroll that is modifying the ability
     */
    public Scroll getScroll() {
        return scroll;
    }

    /**
     * Gets the ability being modified
     * @return The ability being modified
     */
    public CoreAbility getAbility() {
        return ability;
    }

    /**
     * Gets the name of the attribute being modified
     * @return The name of the attribute being modified
     */
    public String getAttributeName() {
        return attributeName;
    }

    /**
     * Gets the original value of the attribute before modification
     * @return The original value of the attribute
     */
    public double getOriginalValue() {
        return originalValue;
    }

    /**
     * Gets the new value of the attribute after modification
     * @return The new value of the attribute
     */
    public double getNewValue() {
        return newValue;
    }

    /**
     * Gets the amount by which the attribute is being modified
     * @return The modification amount (newValue - originalValue)
     */
    public double getModificationAmount() {
        return newValue - originalValue;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
} 