package com.projectkorra.cozmyc.pkscrolls;

import com.projectkorra.cozmyc.pkscrolls.commands.ScrollCommand;
import com.projectkorra.cozmyc.pkscrolls.hooks.ScrollAbilityHooks;
import com.projectkorra.cozmyc.pkscrolls.listeners.EarlyGameListener;
import com.projectkorra.cozmyc.pkscrolls.listeners.EntityDeathListener;
import com.projectkorra.cozmyc.pkscrolls.listeners.LootGenerateListener;
import com.projectkorra.cozmyc.pkscrolls.listeners.ProjectKorraReloadListener;
import com.projectkorra.cozmyc.pkscrolls.listeners.ScrollAttributeListener;
import com.projectkorra.cozmyc.pkscrolls.listeners.ScrollConsumeListener;
import com.projectkorra.cozmyc.pkscrolls.listeners.ScrollUpdateListener;
import com.projectkorra.cozmyc.pkscrolls.listeners.TrialChamberListener;
import com.projectkorra.cozmyc.pkscrolls.managers.ConfigManager;
import com.projectkorra.cozmyc.pkscrolls.managers.PlayerDataManager;
import com.projectkorra.cozmyc.pkscrolls.managers.ScrollManager;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public class ProjectKorraScrolls extends JavaPlugin {

    private static ProjectKorraScrolls instance;

    private ConfigManager configManager;
    private ScrollManager scrollManager;
    private PlayerDataManager playerDataManager;
    private ScrollAbilityHooks abilityHooks;

    @Override
    public void onEnable() {
        instance = this;

        configManager = new ConfigManager(this); // Manages the main configuration with helper methods
        scrollManager = new ScrollManager(this); // Prototyping class for storing and handling scrolls
        playerDataManager = new PlayerDataManager(this); // Manages PDC of players for progress tracking
        abilityHooks = new ScrollAbilityHooks(this); // PK CanBindHook & CanBendHook

        configManager.loadConfig(); // Load main PK Scrolls config
        abilityHooks.registerHooks();
        
        // Register commands
        getCommand("scrolls").setExecutor(new ScrollCommand(this));
        getCommand("scrolls").setTabCompleter(new ScrollCommand(this));
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new ScrollConsumeListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new LootGenerateListener(this), this);
        getServer().getPluginManager().registerEvents(new ProjectKorraReloadListener(this), this);
        getServer().getPluginManager().registerEvents(new TrialChamberListener(this), this);
        getServer().getPluginManager().registerEvents(new EarlyGameListener(this), this);
        getServer().getPluginManager().registerEvents(new ScrollUpdateListener(this), this);
        getServer().getPluginManager().registerEvents(new ScrollAttributeListener(this), this);

        Bukkit.getScheduler().runTaskLater(this, () -> {
            scrollManager.loadAbilities(); // Load scroll configs and add new defaults
        }, 1L);

        getLogger().info("PKScrolls has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("PKScrolls has been disabled!");
    }

    public void debugLog(String message) {
        if (instance.getConfigManager().getConfig().getBoolean("debug.enabled", false)) {
            instance.getLogger().info("[DEBUG] " + message);
        }
    }

    public static ProjectKorraScrolls getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ScrollManager getScrollManager() {
        return scrollManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public NamespacedKey getNamespacedKey(String key) {
        return new NamespacedKey(this, key);
    }
}
