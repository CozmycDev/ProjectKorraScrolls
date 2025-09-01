package com.projectkorra.cozmyc.pkscrolls.listeners;

import com.projectkorra.cozmyc.pkscrolls.ProjectKorraScrolls;
import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ScrollDebugListener implements Listener {
    private final ProjectKorraScrolls plugin;

    public ScrollDebugListener(ProjectKorraScrolls plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!plugin.isDebugging()) {
            return;
        }

        Player player = event.getPlayer();
        BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
        if (bPlayer == null) {
            return;
        }

        // Create debug directories
        File debugDir = new File(plugin.getDataFolder(), "debugging");
        File canBindDir = new File(debugDir, "canbind");
        File canBendDir = new File(debugDir, "canbend");

        if (!debugDir.exists()) {
            debugDir.mkdirs();
        }
        if (!canBindDir.exists()) {
            canBindDir.mkdirs();
        }
        if (!canBendDir.exists()) {
            canBendDir.mkdirs();
        }

        // Create YAML configurations
        YamlConfiguration canBindConfig = new YamlConfiguration();
        YamlConfiguration canBendConfig = new YamlConfiguration();

        // Get all abilities
        List<CoreAbility> allAbilities = new ArrayList<>(CoreAbility.getAbilities());

        // Dump bindable abilities
        for (CoreAbility ability : allAbilities) {
            if (ability == null || !ability.isEnabled()) {
                continue;
            }

            String abilityName = ability.getName();
            boolean canBind = bPlayer.canBind(ability);
            
            // Add ability info to config
            canBindConfig.set(abilityName + ".canBind", canBind);
            canBindConfig.set(abilityName + ".abilityInfo", ability.toString());
            canBindConfig.set(abilityName + ".isEnabled", ability.isEnabled());
            canBindConfig.set(abilityName + ".isHidden", ability.isHiddenAbility());
            canBindConfig.set(abilityName + ".element", ability.getElement().getName());
        }

        // Dump bendable abilities
        for (CoreAbility ability : allAbilities) {
            if (ability == null || !ability.isEnabled()) {
                continue;
            }

            String abilityName = ability.getName();
            boolean canBend = bPlayer.canBend(ability);

            canBendConfig.set(abilityName + ".canBend", canBend);
            canBendConfig.set(abilityName + ".abilityInfo", ability.toString());
            canBendConfig.set(abilityName + ".isEnabled", ability.isEnabled());
            canBendConfig.set(abilityName + ".isHidden", ability.isHiddenAbility());
            canBendConfig.set(abilityName + ".element", ability.getElement().getName());
        }

        try {
            canBindConfig.save(new File(canBindDir, player.getUniqueId() + ".yml"));
            canBendConfig.save(new File(canBendDir, player.getUniqueId() + ".yml"));
            plugin.debugLog("Dumped ability information for player: " + player.getName());
        } catch (IOException e) {
            plugin.debugLog("Failed to save debug files for player " + player.getName() + ": " + e.getMessage());
        }
    }
}
