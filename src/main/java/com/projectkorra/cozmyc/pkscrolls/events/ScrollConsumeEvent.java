package com.projectkorra.cozmyc.pkscrolls.events;

import com.projectkorra.cozmyc.pkscrolls.models.Scroll;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event called when a player consumes a scroll
 */
public class ScrollConsumeEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Scroll scroll;
    private boolean cancelled;

    public ScrollConsumeEvent(Player player, Scroll scroll) {
        this.player = player;
        this.scroll = scroll;
        this.cancelled = false;
    }

    /**
     * Gets the player who consumed the scroll
     * @return The player who consumed the scroll
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the scroll that was consumed
     * @return The scroll that was consumed
     */
    public Scroll getScroll() {
        return scroll;
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
