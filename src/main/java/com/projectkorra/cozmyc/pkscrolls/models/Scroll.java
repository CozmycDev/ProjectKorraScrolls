package com.projectkorra.cozmyc.pkscrolls.models;

import com.projectkorra.cozmyc.pkscrolls.ProjectKorraScrolls;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.CoreAbility;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scroll {

    private final String abilityName;
    private final String displayName;
    private final int modelData;
    private final int unlockCount;
    private final int maxReads;
    private final double defaultWeight;
    private final double mobDropWeightBonus;
    private final double structureLootWeightBonus;
    private final double trialLootWeightBonus;
    private final double unlockedWeight;
    private final boolean canDrop;
    private final boolean canLoot;
    private final boolean canTrialLoot;
    private final String[] description;
    private final CoreAbility coreAbility;
    private final String consumeMessage;
    private final String unlockMessage;
    private final String alreadyUnlockedMessage;
    private final String abilityBoundMessage;
    private final String slotAlreadyBoundMessage;
    private final Map<String, Number> attributes = new HashMap<>();
    private final Map<String, AttributeScaling> attributeScaling = new HashMap<>();

    public Scroll(String abilityName, ConfigurationSection config) {
        this.abilityName = abilityName;
        this.displayName = config.getString("displayName", abilityName);
        this.modelData = config.getInt("modelData", 1);
        this.unlockCount = config.getInt("unlockCount", 1);
        this.maxReads = config.getInt("maxReads", 0);
        this.defaultWeight = config.getDouble("defaultWeight", 1.0);
        this.mobDropWeightBonus = config.getDouble("mobDropWeightBonus", 0.0);
        this.structureLootWeightBonus = config.getDouble("structureLootWeightBonus", 0.0);
        this.trialLootWeightBonus = config.getDouble("trialLootWeightBonus", 0.0);
        this.unlockedWeight = config.getDouble("unlockedWeight", 0.1);
        this.canDrop = config.getBoolean("canDrop", true);
        this.canLoot = config.getBoolean("canLoot", true);
        this.canTrialLoot = config.getBoolean("canTrialLoot", true);
        this.description = config.getStringList("description").toArray(new String[0]);
        this.coreAbility = CoreAbility.getAbility(abilityName);

        loadAttributes(config);

        ConfigurationSection messagesSection = config.getConfigurationSection("messages");
        if (messagesSection != null) {
            this.consumeMessage = messagesSection.getString("consume");
            this.unlockMessage = messagesSection.getString("unlock");
            this.alreadyUnlockedMessage = messagesSection.getString("alreadyUnlocked");
            this.abilityBoundMessage = messagesSection.getString("abilityBound");
            this.slotAlreadyBoundMessage = messagesSection.getString("slotAlreadyBound");
        } else {
            this.consumeMessage = null;
            this.unlockMessage = null;
            this.alreadyUnlockedMessage = null;
            this.abilityBoundMessage = null;
            this.slotAlreadyBoundMessage = null;
        }
    }

    public String getAbilityName() {
        return abilityName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Element getElement() {
        return coreAbility != null ? coreAbility.getElement() : Element.AIR;
    }

    public int getModelData() {
        return modelData;
    }

    public int getUnlockCount() {
        return unlockCount;
    }

    public int getMaxReads() {
        return maxReads;
    }

    public double getDefaultWeight() {
        return defaultWeight;
    }

    public double getMobDropWeightBonus() {
        return mobDropWeightBonus;
    }

    public double getStructureLootWeightBonus() {
        return structureLootWeightBonus;
    }

    public double getTrialLootWeightBonus() {
        return trialLootWeightBonus;
    }

    public double getUnlockedWeight() {
        return unlockedWeight;
    }

    public boolean canDrop() {
        return canDrop;
    }

    public boolean canLoot() {
        return canLoot;
    }

    public boolean canTrialLoot() {
        return canTrialLoot;
    }

    public String[] getDescription() {
        return description;
    }

    public String getConsumeMessage() {
        return consumeMessage;
    }

    public String getUnlockMessage() {
        return unlockMessage;
    }

    public String getAlreadyUnlockedMessage() {
        return alreadyUnlockedMessage;
    }

    public String getAbilityBoundMessage() {
        return abilityBoundMessage;
    }

    public String getSlotAlreadyBoundMessage() {
        return slotAlreadyBoundMessage;
    }

    private void loadAttributes(ConfigurationSection config) {
        if (config == null) return;

        ConfigurationSection attributesSection = config.getConfigurationSection("attributes");
        if (attributesSection == null) return;

        for (String key : attributesSection.getKeys(false)) {
            ConfigurationSection attrSection = attributesSection.getConfigurationSection(key);
            if (attrSection == null) continue;

            Object value = attrSection.get("value");
            if (value != null && Number.class.isAssignableFrom(value.getClass())) {
                Number numberValue = (Number) value;
                attributes.put(key, numberValue);

                ProjectKorraScrolls.getInstance().debugLog("Loaded attribute: " + key +
                        " with value: " + numberValue +
                        " (type: " + value.getClass().getSimpleName() + ")");
            } else {
                ProjectKorraScrolls.getInstance().debugLog("Invalid or non-numeric value for attribute: " + key +
                        " (value: " + value + ")");
            }

            String scalingType = attrSection.getString("type", "NONE").toUpperCase();
            try {
                AttributeScaling scaling = AttributeScaling.valueOf(scalingType);
                attributeScaling.put(key, scaling);
                ProjectKorraScrolls.getInstance().debugLog("Set scaling type for " + key + ": " + scaling);
            } catch (IllegalArgumentException e) {
                attributeScaling.put(key, AttributeScaling.NONE);
                ProjectKorraScrolls.getInstance().debugLog("Invalid scaling type '" + scalingType +
                        "' for attribute " + key + ", defaulting to NONE");
            }
        }
    }

    public Number calculateScaledAttribute(String attribute, Number baseValue, int postUnlockProgress) {
        if (!attributes.containsKey(attribute) || !attributeScaling.containsKey(attribute)) {
            return baseValue;
        }

        AttributeScaling scaling = attributeScaling.get(attribute);
        if (scaling == AttributeScaling.NONE) {
            return baseValue;
        }

        // Get the base increment/factor from the config
        Number configValue = attributes.get(attribute);
        
        // Calculate the total modification based on scaling type and post-unlock progress
        double totalModification = switch (scaling) {
            case ADDITIVE -> configValue.doubleValue() * postUnlockProgress; // Fixed increment per level
            case MULTIPLICATIVE -> baseValue.doubleValue() * (configValue.doubleValue() * postUnlockProgress); // Percentage increase per level
            case EXPONENTIAL -> baseValue.doubleValue() * (Math.pow(1 + configValue.doubleValue(), postUnlockProgress) - 1); // Compound increase per level
            case NONE -> 0.0;
        };

        if (baseValue instanceof Long) {
            return baseValue.longValue() + (long) totalModification;
        } else if (baseValue instanceof Integer) {
            return baseValue.intValue() + (int) totalModification;
        } else {
            return baseValue.doubleValue() + totalModification;
        }
    }

    public enum AttributeScaling {
        ADDITIVE,      // Fixed increment per level (e.g., +2.0 per level)
        MULTIPLICATIVE, // Percentage increase per level (e.g., +10% per level)
        EXPONENTIAL,   // Compound increase per level (e.g., 10% compound per level)
        NONE;         // No scaling
    }
}
