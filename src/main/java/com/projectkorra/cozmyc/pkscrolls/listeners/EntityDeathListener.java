package com.projectkorra.cozmyc.pkscrolls.listeners;

import com.projectkorra.cozmyc.pkscrolls.ProjectKorraScrolls;
import com.projectkorra.cozmyc.pkscrolls.models.Scroll;
import com.projectkorra.cozmyc.pkscrolls.utils.ColorUtils;
import com.projectkorra.cozmyc.pkscrolls.utils.ScrollItemFactory;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Random;

public class EntityDeathListener implements Listener {

    private final ProjectKorraScrolls plugin;
    private final Random random;

    public EntityDeathListener(ProjectKorraScrolls plugin) {
        this.plugin = plugin;
        this.random = new Random();
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!plugin.getConfigManager().getConfig().getBoolean("settings.naturalDrops.enabled", true)) {
            ProjectKorraScrolls.getInstance().debugLog("Natural drops are disabled");
            return;
        }

        EntityType entityType = event.getEntityType();
        if (plugin.getConfigManager().getConfig().getBoolean("settings.naturalDrops.hostileMobsOnly", true) 
                && !isHostileMob(entityType)) {
            ProjectKorraScrolls.getInstance().debugLog("Entity " + entityType + " is not hostile, skipping drop");
            return;
        }

        double chance = plugin.getConfigManager().getConfig().getDouble("settings.naturalDrops.chance", 0.1);
        double roll = random.nextDouble();
        ProjectKorraScrolls.getInstance().debugLog("Drop chance roll: " + roll + " (needed: " + chance + ")");
        if (roll > chance) {
            ProjectKorraScrolls.getInstance().debugLog("Drop chance not met for " + entityType);
            return;
        }

        Player killer = event.getEntity().getKiller();
        if (killer == null) {
            ProjectKorraScrolls.getInstance().debugLog("No player killer for " + entityType);
            return;
        }

        Scroll selectedScroll = plugin.getScrollManager().getRandomScrollForPlayer(killer, false, false, true, false);
        if (selectedScroll == null) {
            ProjectKorraScrolls.getInstance().debugLog("Failed to get random scroll for player");
        }

        ItemStack scrollItem = ScrollItemFactory.createScroll(selectedScroll);
        event.getDrops().add(scrollItem);

        String dropMessage = plugin.getConfigManager().getMessage("naturalDrop");
        if (plugin.getConfigManager().getConfig().getBoolean("naturalDrops.showDropMessage", true) 
                && dropMessage != null && !dropMessage.trim().isEmpty()) {
            killer.sendMessage(ColorUtils.formatMessage(
                dropMessage
                    .replace("%entity%", entityType.name())
                    .replace("%scroll%", selectedScroll.getDisplayName())
            ));
        } else {
            ProjectKorraScrolls.getInstance().debugLog("Skipping drop message (disabled or empty)");
        }
    }

    private boolean isHostileMob(EntityType type) {
        List<String> hostileMobs = plugin.getConfigManager().getConfig().getStringList("settings.naturalDrops.hostileMobs");
        if (hostileMobs.isEmpty()) {
            return switch (type) {
                case ZOMBIE, SKELETON, SPIDER, CREEPER, ENDERMAN, WITCH, SLIME, PHANTOM, DROWNED, HUSK, STRAY, 
                     CAVE_SPIDER, SILVERFISH, ENDERMITE, MAGMA_CUBE, BLAZE, GHAST, SHULKER, HOGLIN, ZOGLIN, 
                     PIGLIN_BRUTE, RAVAGER, VINDICATOR, PILLAGER, EVOKER, VEX -> true;
                default -> false;
            };
        }
        return hostileMobs.contains(type.name());
    }
}
