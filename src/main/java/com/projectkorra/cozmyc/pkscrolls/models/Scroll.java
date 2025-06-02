package com.projectkorra.cozmyc.pkscrolls.models;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.CoreAbility;
import org.bukkit.configuration.ConfigurationSection;

public class Scroll {

    private final String abilityName;
    private final String displayName;
    private final int modelData;
    private final int unlockCount;
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

    public Scroll(String abilityName, ConfigurationSection config) {
        this.abilityName = abilityName;
        this.displayName = config.getString("displayName", abilityName);
        this.modelData = config.getInt("modelData", 1);
        this.unlockCount = config.getInt("unlockCount", 1);
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
}
