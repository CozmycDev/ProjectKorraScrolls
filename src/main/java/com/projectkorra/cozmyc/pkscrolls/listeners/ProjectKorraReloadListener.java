package com.projectkorra.cozmyc.pkscrolls.listeners;

import com.projectkorra.cozmyc.pkscrolls.ProjectKorraScrolls;
import com.projectkorra.cozmyc.pkscrolls.commands.ScrollCommand;
import com.projectkorra.projectkorra.event.BendingReloadEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ProjectKorraReloadListener implements Listener {

    private final ProjectKorraScrolls plugin;

    public ProjectKorraReloadListener(ProjectKorraScrolls plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onProjectKorraReload(BendingReloadEvent event) {
        Bukkit.getScheduler().runTask(plugin, () -> ScrollCommand.handleReload(plugin, event.getSender()));
    }
}
