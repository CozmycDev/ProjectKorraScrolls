package com.projectkorra.cozmyc.pkscrolls.managers;

import com.projectkorra.cozmyc.pkscrolls.ProjectKorraScrolls;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ConfigManager {

    private final ProjectKorraScrolls plugin;
    private FileConfiguration config;
    private File configFile;

    public ConfigManager(ProjectKorraScrolls plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }

        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public String getMessage(String path) {
        return config.getString("messages." + path, "Message not found: " + path);
    }

    public int getInt(String path, int defaultValue) {
        return config.getInt(path, defaultValue);
    }
}
