package com.projectkorra.cozmyc.pkscrolls.managers;

import com.projectkorra.cozmyc.pkscrolls.ProjectKorraScrolls;
import com.projectkorra.cozmyc.pkscrolls.models.Scroll;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PlayerDataManager {

    private final ProjectKorraScrolls plugin;

    public PlayerDataManager(ProjectKorraScrolls plugin) {
        this.plugin = plugin;
    }

    public boolean hasAbilityUnlocked(Player player, String abilityName) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        if (pdc.has(plugin.getNamespacedKey("unlocked_" + abilityName), PersistentDataType.BYTE)) {
            return true;
        }
        return false;
    }

    // Return true if this unlocks the ability
    public boolean consumeScroll(Player player, String abilityName) {
        ProjectKorraScrolls.getInstance().debugLog("Processing scroll consumption for " + player.getName() + ": " + abilityName);
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        int currentProgress = pdc.getOrDefault(plugin.getNamespacedKey("progress_" + abilityName), PersistentDataType.INTEGER, 0);
        Scroll scroll = plugin.getScrollManager().getScroll(abilityName);
        if (scroll == null) {
            return false;
        }
        int required = scroll.getUnlockCount();
        currentProgress++;
        pdc.set(plugin.getNamespacedKey("progress_" + abilityName), PersistentDataType.INTEGER, currentProgress);
        if (currentProgress >= required) {
            ProjectKorraScrolls.getInstance().debugLog("Player " + player.getName() + " unlocked ability: " + abilityName);
            pdc.set(plugin.getNamespacedKey("unlocked_" + abilityName), PersistentDataType.BYTE, (byte) 1);
        }
        return currentProgress >= required;
    }

    public void resetProgress(Player player, String abilityName) {
        ProjectKorraScrolls.getInstance().debugLog("Resetting progress for " + player.getName() + ": " + abilityName);
        setProgress(player, abilityName, 0);
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        pdc.remove(plugin.getNamespacedKey("unlocked_" + abilityName));
    }

    public void resetAllProgress(Player player) {
        ProjectKorraScrolls.getInstance().debugLog("Resetting all progress for " + player.getName());
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        ArrayList<NamespacedKey> keysToRemove = new ArrayList<>();
        for (NamespacedKey key : pdc.getKeys()) {
            String keyString = key.getKey();
            if (keyString.startsWith("unlocked_") || keyString.startsWith("progress_")) {
                keysToRemove.add(key);
            }
        }
        for (NamespacedKey key : keysToRemove) {
            pdc.remove(key);
        }
    }

    public void resetPlayer(Player player) {
        ProjectKorraScrolls.getInstance().debugLog("Resetting player: " + player.getName());
        resetAllProgress(player);
        resetEarlyGameProgress(player);
    }

    public void resetEarlyGameProgress(Player player) {
        player.getPersistentDataContainer().remove(plugin.getNamespacedKey("first_log_break"));
        player.getPersistentDataContainer().remove(plugin.getNamespacedKey("first_crop_plant"));
        player.getPersistentDataContainer().remove(plugin.getNamespacedKey("first_bed_sleep"));
        player.getPersistentDataContainer().remove(plugin.getNamespacedKey("received_abilities"));
        ProjectKorraScrolls.getInstance().debugLog("Reset early game progress for player: " + player.getName());
    }

    public Map<String, Integer> getProgress(Player player) {
        Map<String, Integer> progress = new HashMap<>();
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        for (String abilityName : plugin.getScrollManager().getAbilityNames()) {
            NamespacedKey key = plugin.getNamespacedKey("progress_" + abilityName);
            progress.put(abilityName, pdc.getOrDefault(key, PersistentDataType.INTEGER, 0));
        }
        return progress;
    }

    public void setProgress(Player player, String abilityName, int progress) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        pdc.set(plugin.getNamespacedKey("progress_" + abilityName), PersistentDataType.INTEGER, progress);
    }

    // legacy updater from the early versions
    public static void updateProgress(Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        NamespacedKey migratedKey = ProjectKorraScrolls.getInstance().getNamespacedKey("data_migrated");

        if (pdc.has(migratedKey, PersistentDataType.BYTE)) {
            return;
        }

        File file = new File(ProjectKorraScrolls.getInstance().getDataFolder(), "playerdata.yml");
        if (!file.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        String uuid = player.getUniqueId().toString();

        if (!config.contains("players." + uuid)) return;

        ConfigurationSection section = config.getConfigurationSection("players." + uuid);
        if (section == null) return;

        for (String abilityName : section.getKeys(false)) {
            int progress = section.getInt(abilityName);
            for (int i = 0; i < progress; i++) {
                ProjectKorraScrolls.getInstance().getPlayerDataManager().consumeScroll(player, abilityName);
            }
        }

        pdc.set(migratedKey, PersistentDataType.BYTE, (byte) 1);
    }
}
