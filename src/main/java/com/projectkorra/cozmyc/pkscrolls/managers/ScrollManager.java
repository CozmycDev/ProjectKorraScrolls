package com.projectkorra.cozmyc.pkscrolls.managers;

import com.projectkorra.cozmyc.pkscrolls.ProjectKorraScrolls;
import com.projectkorra.cozmyc.pkscrolls.models.Scroll;
import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.MultiAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class ScrollManager {

    private final ProjectKorraScrolls plugin;
    private final Map<String, Scroll> scrolls = new HashMap<>();
    private final Random random;

    public ScrollManager(ProjectKorraScrolls plugin) {
        this.plugin = plugin;
        this.random = new Random();
    }

    public void loadAbilities() {
        ProjectKorraScrolls.getInstance().debugLog("Loading abilities from ProjectKorra");
        scrolls.clear();

        File scrollsDir = new File(plugin.getDataFolder(), "scrolls");
        if (!scrollsDir.exists()) {
            scrollsDir.mkdirs();
        }

        for (CoreAbility ability : CoreAbility.getAbilities()) {
            if (ability.isHiddenAbility() || !ability.isEnabled()) {
                continue;
            }

            String abilityName = ability.getName();
            Element element = ability.getElement();
            String elementName = element.getName();

            File elementDir = new File(scrollsDir, elementName);
            if (!elementDir.exists()) {
                elementDir.mkdirs();
            }

            File scrollFile = new File(elementDir, abilityName + ".yml");
            if (!scrollFile.exists()) {
                ProjectKorraScrolls.getInstance().debugLog("Creating new scroll config for ability: " + abilityName + " in element: " + elementName);
                updateScrollConfig(ability);
            } else {
                YamlConfiguration scrollConfig = YamlConfiguration.loadConfiguration(scrollFile);
                ConfigurationSection abilitySection = scrollConfig.getConfigurationSection("");
                
                boolean needsUpdate = false;
                if (!abilitySection.contains("displayName")) needsUpdate = true;
                if (!abilitySection.contains("description")) needsUpdate = true;
                if (!abilitySection.contains("unlockCount")) needsUpdate = true;
                if (!abilitySection.contains("canDrop")) needsUpdate = true;
                if (!abilitySection.contains("canLoot")) needsUpdate = true;
                if (!abilitySection.contains("canTrialLoot")) needsUpdate = true;
                if (!abilitySection.contains("modelData")) needsUpdate = true;
                
                if (needsUpdate) {
                    ProjectKorraScrolls.getInstance().debugLog("Updating missing fields for ability: " + abilityName);
                    updateScrollConfig(ability);
                } else {
                    scrolls.put(abilityName, new Scroll(abilityName, abilitySection));
                    ProjectKorraScrolls.getInstance().debugLog("Loaded existing config for ability: " + abilityName);
                }
            }
        }
        ProjectKorraScrolls.getInstance().debugLog("Finished loading abilities. Total scrolls: " + scrolls.size());
    }

    private void updateScrollConfig(CoreAbility ability) {
        String abilityName = ability.getName();
        Element element = ability.getElement();
        String elementName = element.getName();

        int modelData = getDefaultModelDataForElement(elementName);
        int unlockCount = getDefaultUnlockCountForAbility(ability, elementName);

        File scrollFile = new File(plugin.getDataFolder(), 
            "scrolls/" + elementName + "/" + abilityName + ".yml");
        YamlConfiguration scrollConfig = YamlConfiguration.loadConfiguration(scrollFile);
        
        if (!scrollConfig.contains("displayName")) {
            scrollConfig.set("displayName", abilityName);
        }
        if (!scrollConfig.contains("description")) {
            String elementColor = plugin.getConfigManager().getConfig().getString("elementColors." + elementName, "&7");
            List<String> defaultDescription = List.of(
                "&7This scroll contains knowledge related to the ability " + elementColor + abilityName,
                "&7Learn more about it using &8[&b/b help " + abilityName + "&8]"
            );
            scrollConfig.set("description", defaultDescription);
        }
        if (!scrollConfig.contains("unlockCount")) {
            scrollConfig.set("unlockCount", unlockCount);
        }
        if (!scrollConfig.contains("canDrop")) {
            scrollConfig.set("canDrop", true);
        }
        if (!scrollConfig.contains("canLoot")) {
            scrollConfig.set("canLoot", true);
        }
        if (!scrollConfig.contains("canTrialLoot")) {
            scrollConfig.set("canTrialLoot", true);
        }
        if (!scrollConfig.contains("modelData")) {
            scrollConfig.set("modelData", modelData);
        }
        if (!scrollConfig.contains("mobDropWeightBonus")) {
            scrollConfig.set("mobDropWeightBonus", 0.0);
        }
        if (!scrollConfig.contains("structureLootWeightBonus")) {
            scrollConfig.set("structureLootWeightBonus", 0.0);
        }
        if (!scrollConfig.contains("trialLootWeightBonus")) {
            scrollConfig.set("trialLootWeightBonus", 0.0);
        }
        if (!scrollConfig.contains("unlockedWeight")) {
            scrollConfig.set("unlockedWeight", 0.1);
        }
        if (!scrollConfig.contains("defaultWeight")) {
            scrollConfig.set("defaultWeight", 1.0);
        }

        try {
            scrollConfig.save(scrollFile);
            scrolls.put(abilityName, new Scroll(abilityName, scrollConfig));
            ProjectKorraScrolls.getInstance().debugLog("Updated and saved config for scroll: " + abilityName);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save scroll config for " + abilityName + ": " + e.getMessage());
        }
    }

    private int getDefaultModelDataForElement(String elementName) {
        return switch (elementName.toUpperCase()) {
            case "WATER" -> 3001;
            case "ICE" -> 3003;
            case "HEALING" -> 3005;
            case "PLANT" -> 3002;
            case "BLOOD" -> 3004;
            case "EARTH" -> 4001;
            case "LAVA" -> 4004;
            case "METAL" -> 4003;
            case "SAND" -> 4002;
            case "FIRE" -> 5001;
            case "LIGHTNING" -> 5002;
            case "COMBUSTION" -> 5005;
            case "AIR" -> 6001;
            case "FLIGHT" -> 6002;
            case "SPIRITUAL" -> 6003;
            case "SOUND" -> 6004;
            case "CHI" -> 7001;
            case "AVATAR" -> 8001;
            default -> 9001;
        };
    }

    private int getDefaultUnlockCountForAbility(CoreAbility ability, String elementName) {
        int unlockCount = 2;
        if (ability instanceof AddonAbility) {
            unlockCount = 4;
        }
        if (ability instanceof ComboAbility) {
            unlockCount = 3;
        }
        if (ability instanceof MultiAbility) {
            unlockCount = 5;
        }
        if (ability instanceof PassiveAbility) {
            unlockCount = 1;
        }
        if (elementName.equalsIgnoreCase("AVATAR")) {
            unlockCount = 10;
        }
        return unlockCount;
    }

    public Scroll getScroll(String abilityName) {
        return scrolls.get(abilityName);
    }

    public Set<String> getAbilityNames() {
        return new HashSet<>(scrolls.keySet());
    }

    public List<Scroll> getScrolls() {
        return new ArrayList<>(scrolls.values());
    }

    // Filter the input scroll list by the players elements *with respect to the configuration*
    public List<Scroll> filterScrollsByPlayerElements(Player player, List<Scroll> scrolls, String configPath) {
        boolean playerElementOnly = plugin.getConfigManager().getConfig().getBoolean(configPath + ".playerElementOnly", true);
        boolean includeSubelements = plugin.getConfigManager().getConfig().getBoolean(configPath + ".includeSubelements", true);
        
        if (!playerElementOnly) {
            ProjectKorraScrolls.getInstance().debugLog("Player element filtering disabled, returning all scrolls");
            return new ArrayList<>(scrolls);
        }

        if (player == null) {
            ProjectKorraScrolls.getInstance().debugLog("No player context, returning all scrolls");
            return new ArrayList<>(scrolls);
        }

        BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
        if (bPlayer == null) {
            ProjectKorraScrolls.getInstance().debugLog("Could not get BendingPlayer for " + player.getName());
            return new ArrayList<>(scrolls);
        }

        List<Element> playerElements = new ArrayList<>(bPlayer.getElements());
        if (includeSubelements) {
            playerElements.addAll(bPlayer.getSubElements());
        }
        if (playerElements.isEmpty() && plugin.getConfigManager().getConfig().getBoolean("settings.elementSpecific.randomIfNoElement")) {
            return new ArrayList<>(scrolls); // Return scrolls for all elements, no filtering applicable for this player
        }

        ProjectKorraScrolls.getInstance().debugLog("Player elements: " + playerElements);
        List<Scroll> elementScrolls = scrolls.stream()
            .filter(scroll -> playerElements.contains(scroll.getElement()))
            .toList();

        ProjectKorraScrolls.getInstance().debugLog("Found " + elementScrolls.size() + " element-specific scrolls");
        return elementScrolls;
    }

    public List<Scroll> getLootableScrolls() {
        return scrolls.values().stream()
            .filter(Scroll::canLoot)
            .toList();
    }

    public List<Scroll> getTrialLootableScrolls() {
        return scrolls.values().stream()
            .filter(Scroll::canTrialLoot)
            .toList();
    }

    public List<Scroll> getDroppableScrolls() {
        return scrolls.values().stream()
            .filter(Scroll::canDrop)
            .toList();
    }

    // The weight spread is over whatever filterScrollsByPlayerElements returns
    // prototyping!! ScrollManager will eventually be replaced with more robust methods for generating scrolls
    public Scroll getRandomScrollForPlayer(Player player, boolean isLootEvent, boolean isTrialEvent, boolean isDropEvent, boolean isEarlyGameEvent) {
        ProjectKorraScrolls.getInstance().debugLog("Getting random scroll for player: " + player.getName());

        List<Scroll> availableScrolls = getScrolls();

        if (isDropEvent) {
            availableScrolls = filterScrollsByPlayerElements(player, getDroppableScrolls(), "settings.naturalDrops");
        }
        if (isLootEvent) {
            availableScrolls = filterScrollsByPlayerElements(player, getLootableScrolls(), "settings.lootGeneration");
        }
        if (isTrialEvent) {
            availableScrolls = filterScrollsByPlayerElements(player, getTrialLootableScrolls(), "settings.trialLoot");
        }
        if (isEarlyGameEvent) {
            availableScrolls = filterScrollsByPlayerElements(player, availableScrolls, "settings.trialLoot");
        }

        if (availableScrolls.isEmpty()) {
            ProjectKorraScrolls.getInstance().debugLog("No available scrolls for player");
            return null;
        }

        List<WeightedScroll> weightedScrolls = new ArrayList<>();
        for (Scroll scroll : availableScrolls) {
            double weight = scroll.getDefaultWeight();
            if (plugin.getPlayerDataManager().getProgress(player).getOrDefault(scroll.getAbilityName(), 0) >= scroll.getUnlockCount()) {
                weight = scroll.getUnlockedWeight();
            }

            if (isDropEvent) {
                weight += scroll.getMobDropWeightBonus();
            }
            if (isLootEvent) {
                weight += scroll.getStructureLootWeightBonus();
            }
            if (isTrialEvent) {
                weight += scroll.getTrialLootWeightBonus();
            }

            weightedScrolls.add(new WeightedScroll(scroll, weight));
        }

        double totalWeight = weightedScrolls.stream()
            .mapToDouble(WeightedScroll::weight)
            .sum();

        double randomValue = random.nextDouble() * totalWeight;
        double currentWeight = 0;

        for (WeightedScroll weightedScroll : weightedScrolls) {
            currentWeight += weightedScroll.weight();
            if (randomValue <= currentWeight) {
                ProjectKorraScrolls.getInstance().debugLog("Selected scroll: " + weightedScroll.scroll().getDisplayName());
                return weightedScroll.scroll();
            }
        }

        ProjectKorraScrolls.getInstance().debugLog("No scroll selected");
        return null;
    }

    private record WeightedScroll(Scroll scroll, double weight) {}
}
