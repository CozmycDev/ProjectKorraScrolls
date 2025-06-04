[![GitHub Pre-Release](https://img.shields.io/github/release-pre/CozmycDev/ProjectKorraScrolls.svg)](https://github.com/CozmycDev/ProjectKorraScrolls/releases)
[![Github All Releases](https://img.shields.io/github/downloads/CozmycDev/ProjectKorraScrolls/total.svg)](https://github.com/CozmycDev/ProjectKorraScrolls/releases)
![Size](https://img.shields.io/github/repo-size/CozmycDev/ProjectKorraScrolls.svg)

# ProjectKorraScrolls

**ProjectKorraScrolls** is a Minecraft plugin designed for servers using the [ProjectKorra](https://projectkorra.com/) bending plugin. It introduces an immersive progression mechanic where players must discover and collect "scrolls" to unlock bending abilities. These scrolls can be obtained through various in-game activities, adding depth and exploration to the bending experience.

---

## 🌟 Features

### 🔓 Ability Unlocking via Scrolls

* **Scroll Collection**: Players must gather a specific number of scrolls corresponding to an ability to unlock it.
* **Progress Tracking**: Each scroll read contributes to the unlocking progress of its respective ability.
* **Element-Specific Scrolls**: Generated scrolls can be filtered by the players' elements (e.g., Air, Water, Earth, Fire) and sub-elements (e.g., Lightning, Metal), ensuring players collect more scrolls relevant to their bending path.

### 🧭 Diverse Scroll Acquisition Methods

* **Mob Drops**: Hostile mobs have a configurable chance to drop scrolls upon defeat.
* **Loot Chests**: Scrolls can be found in naturally generated chests within structures.
* **Trial Vaults**: Special vaults that, when unlocked, guarantee scroll rewards.
* **Early Game Rewards**: New players receive scrolls for completing initial milestones like breaking their first log, sleeping in a bed, or planting a crop.

### ⚙️ Extensive Configuration Options

* **Global Settings**: Adjust the overall behavior of scroll drops, loot generation, and trial rewards.
* **Ability-Specific Settings**: Fine-tune individual abilities with parameters like unlock count, drop eligibility, and loot weights.
* **Message Customization**: Personalize in-game messages related to scrolls and abilities.

---

## ⚙️ Configuration Overview

### 🔧 Global Settings

Configure overarching behaviors for scroll distribution and player progression. By default, nothing is enabled and no scrolls are generated until you enable something.

```yaml
# ProjectKorraScrolls Configuration

# Debug settings
debug:
  enabled: false  # Enable detailed debug logging
  logLevel: "INFO"  # Log level for debug messages (INFO, WARNING, SEVERE)

# Global settings
settings:
  # Ability Levelling
  abilityLevelling:
    enabled: false # Globally toggles the per scrolls maxReads and attribute scaling system

  # Element-specific settings
  elementSpecific:
    randomIfNoElement: true  # Whether to give random scrolls if player has no element, disables scroll generations for non-benders if false

  # Early Game Rewards - give new players a small boost with rewards specific to their element
  earlyGameRewards:
    enabled: false  # Whether to give players bonus scrolls for their first log broken, crop planted, and bed slept in.
    amount: 3  # Number of scrolls to give for each milestone (always identical copies)
    playerElementOnly: true  # Whether these should be scrolls from player's elements only
    includeSubelements: false  # Whether to include subelement scrolls when filtering by element

  # Natural drops from mobs
  naturalDrops:
    enabled: false # Whether to enable natural drops from mobs
    hostileMobsOnly: true  # Only hostile mobs can drop scrolls. Spawner/Spawn Egg mobs never count.
    chance: 0.1  # # Initial roll chance for a mob to drop a scroll (10%)
    playerElementOnly: true  # Whether mobs should drop scrolls from player's elements only
    includeSubelements: true  # Whether to include subelement scrolls when filtering by element

  # Loot chest generation
  lootGeneration:
    enabled: false # Whether to enable loot chest generation
    chance: 0.25  # Initial roll chance for a chest to contain scrolls (25%)
    maxScrollsPerChest: 2  # The max number of scrolls to generate per chest
    playerElementOnly: true  # Only generate scrolls for player's element
    includeSubelements: true  # Whether to include subelement scrolls when filtering by element

  # Trial vault generation
  trialLoot:
    enabled: false # Whether to enable trial vault generation
    chance: 1.0  # # Initial roll chance for a trial vault to contain scrolls (100%)
    maxScrollsPerChest: 1  # The max number of scrolls to generate per unlocked vault
    playerElementOnly: true  # Only generate scrolls for player's element
    includeSubelements: true  # Whether to include subelement scrolls when filtering by element

# Element color mappings
elementColors:
  AIR: "&f"
  WATER: "&b"
  EARTH: "&2"
  FIRE: "&c"
  CHI: "&e"
  AVATAR: "&d"
  ICE: "&b"
  LAVA: "&6"
  METAL: "&7"
  SAND: "&e"
  LIGHTNING: "&e"
  COMBUSTION: "&c"
  FLIGHT: "&f"
  SPIRITUAL: "&d"
  HEALING: "&a"
  PLANT: "&a"
  BLOOD: "&4"

# Messages
messages:
  prefix: "&8[&ePK&bScrolls&8] &r"

  # General messages
  noPermission: "&cYou don't have permission to use this command."
  invalidPlayer: "&cInvalid player name."
  invalidAbility: "&cInvalid ability name."
  invalidAmount: "&cInvalid amount!"
  configReloaded: "&aConfiguration reloaded!"
  reloadComplete: "&aReload complete!"

  # Scroll consume messages
  scrollConsumed: "&aYou read the %ability%&a scroll! &8[&e%progress%&7/&e%total%&8]"
  abilityUnlocked: "&aYou've learned how to use &e%ability%&a!"
  alreadyUnlocked: "&cYou already know this ability!"
  abilityBound: "&7Unbind this ability with &8[&b/b clear %ability% %slot%&8]"
  slotAlreadyBound: "&7Bind it to an available slot with &8[&b/b bind %ability%&8]"
  maxReadsReached: "&cYou've reached the maximum number of reads for &e%ability%&c!"

  # Scroll give/receive messages
  scrollGiven: "&aGiven &e%amount% &a%ability%&a scroll(s) to &e%player%&a!"
  scrollReceived: "&aYou received &e%amount%&a %ability% &ascroll(s)!"

  # Progress messages
  progressReset: "&aProgress for &e%ability%&a has been reset for &e%player%&a!"
  progressResetAll: "&aAll progress has been reset for &e%player%&a!"

  # Early game messages
  earlyGameReward: "&aYou found &e%amount% &a%ability% &ascrolls!"

  # Drop messages
  naturalDrop: "&aThe &e%entity%&a dropped a(n) &e%scroll% &ascroll!"
  trialDrop: ""
  chestDrop: ""

  # Command messages
  commands:
    give:
      usage: "&cUsage: /scroll give <player> <ability> [amount]"
      success: "&aGave %amount% %scroll% scroll(s) to %player%"
    progress:
      header: "&8&m-----------------------------------------------------"
      title: "&b&lScroll Progress"
      elementHeader: "&8&l[&b%element%&8&l]"
      unlockedPrefix: "&a✓"
      lockedPrefix: "&c✗"
      abilityFormat: "%prefix% &7%name%%progress%"
      noProgress: "&cYou haven't made progress on any abilities yet."
      scrollNotFound: "&cScroll not found for ability: %ability%"
      progressFormat: " &8[&e%current%&7/&e%required%&8]"
      maxReadsFormat: " &8[&e%current%&7/&e%max%&8]"
      itemsPerPage: 10
      pageInfo: "&bPage %current%&7/&b%total%"
      prevButton: " &8[&3&l<&8]"
      nextButton: " &8[&3&l>&8]"
      prevButtonHover: "&7Click to go to previous page"
      nextButtonHover: "&7Click to go to next page"
      pageUsage: "&cUsage: /scroll page <number|next|prev>"
      invalidPage: "&cInvalid page number."
      noPages: "&cThere are no pages to display."
    reset:
      usage: "&cUsage: /scroll reset <player> [ability]"
      success: "&aReset progress for %player%"
      abilitySuccess: "&aReset progress for %ability% for %player%"
    resetEarlyGame:
      usage: "&cUsage: /scroll resetearlygame <player>"
      success: "&aReset early game progress for %player%"
    resetProgress:
      usage: "&cUsage: /scroll resetprogress <player>"
      success: "&aReset all progress for %player%"
    reload:
      success: "PKScrolls has been reloaded"
    help:
      header: "&8&m-----------------------------------------------------"
      title: "&b&lScroll Commands"
      footer: "&8&m-----------------------------------------------------"
      commands:
        give: "&b/scroll give <player> <ability> [amount] &7- Give a scroll to a player"
        progress: "&b/scroll progress [player] &7- View scroll progress"
        reset: "&b/scroll reset <player> [ability] &7- Reset progress"
        reload: "&b/scroll reload &7- Reload the plugin"
        resetEarlyGame: "&b/scroll resetearlygame <player> &7- Reset early game progress"
        resetProgress: "&b/scroll resetprogress <player> &7- Reset all progress"
        page: "&b/scroll page <number> &7- Navigate to a specific page"

# Natural drops settings
naturalDrops:
  showDropMessage: true
  hostileMobs:
    - "ZOMBIE"
    - "SKELETON"
    - "SPIDER"
    - "CREEPER"
    - "ENDERMAN"
    - "WITCH"
    - "SLIME"
    - "PHANTOM"
    - "DROWNED"
    - "HUSK"
    - "STRAY"
    - "CAVE_SPIDER"
    - "SILVERFISH"
    - "ENDERMITE"
    - "MAGMA_CUBE"
    - "BLAZE"
    - "GHAST"
    - "SHULKER"
    - "HOGLIN"
    - "ZOGLIN"
    - "PIGLIN_BRUTE"
    - "RAVAGER"
    - "VINDICATOR"
    - "PILLAGER"
    - "EVOKER"
    - "VEX"

# Trial chamber settings
trialLoot:
  showDropMessage: false

# Chest loot settings
chestLoot:
  showDropMessage: false
```

### 📜 Ability-Specific Configuration

Each ability has its own configuration file located in `/scrolls/<Element>/<Ability>.yml`. Here's an example for `AirBlast`:

```yaml
displayName: AirBlast
description:
  - '&7This scroll contains knowledge related to the ability &7AirBlast'
  - '&7Learn more about it using &8[&b/b help AirBlast&8]'
unlockCount: 2
maxReads: 6
canDrop: true
canLoot: true
canTrialLoot: true
modelData: 6001
mobDropWeightBonus: 0.0
structureLootWeightBonus: 0.0
trialLootWeightBonus: 0.0
unlockedWeight: 0.1
defaultWeight: 1.0
attributes:
  Speed:
    type: additive
    value: 0.0
  Radius:
    type: additive
    value: 0.0
  SelfPush:
    type: additive
    value: 0.0
  Cooldown:
    type: additive
    value: 0
  Knockback:
    type: additive
    value: 0.0
  Range:
    type: additive
    value: 0.0
  Damage:
    type: additive
    value: 0.0
messages:
  consume: '&aYou read the %ability%&a scroll! &8[&e%progress%&7/&e%total%&8]'
  unlock: '&aYou''ve learned how to use &e%ability%&a!'
  alreadyUnlocked: '&cYou already know this ability!'
  abilityBound: '&7Unbind this ability with &8[&b/b clear %ability% %slot%&8]'
  slotAlreadyBound: '&7Bind it to an available slot with &8[&b/b bind %ability%&8]'
```

**Parameters Explained**:

* `displayName`: Custom display name for the scroll (defaults to ability name)
* `description`: List of description lines for the scroll
* `unlockCount`: Number of scrolls required to unlock the ability
* `maxReads`: Maximum number of times a scroll can be read after unlocking (0 = unlimited)
* `canDrop`: Determines if the scroll can drop from mobs
* `canLoot`: Determines if the scroll can be found in chests
* `canTrialLoot`: Determines if the scroll can be obtained from trial vaults
* `modelData`: Custom model data for resource pack integration
* `mobDropWeightBonus`: Increases the likelihood of the scroll dropping from mobs
* `structureLootWeightBonus`: Increases the likelihood of the scroll appearing in structure chests
* `trialLootWeightBonus`: Increases the likelihood of the scroll appearing in trial vaults
* `unlockedWeight`: Weight for scroll selection after the ability is unlocked
* `defaultWeight`: Base weight for scroll selection

**Attribute Scaling Types**:

Each attribute can have one of the following scaling types:

* `additive`: Fixed increment per level (e.g., +2.0 per level)
  * Formula: `baseValue + (configValue * postUnlockProgress)`
  * Example: If `value: 2.0`, each level adds 2.0 to the base value

* `multiplicative`: Percentage increase per level (e.g., +10% per level)
  * Formula: `baseValue * (1 + (configValue * postUnlockProgress))`
  * Example: If `value: 0.1`, each level adds 10% to the base value

* `exponential`: Compound increase per level (e.g., 10% compound per level)
  * Formula: `baseValue * (1 + configValue)^postUnlockProgress`
  * Example: If `value: 0.1`, each level multiplies the previous value by 1.1

* `NONE`: No scaling applied, same as additive: 0

**Messages**:

Custom messages for various scroll interactions. Available placeholders:
* `%ability%`: The ability name
* `%progress%`: Current progress towards unlocking
* `%total%`: Total scrolls needed to unlock
* `%slot%`: The slot number for binding messages

---

## 🛠️ Commands

* `/scroll give <player> <ability> [amount]`: Give scrolls to a player.
* `/scroll progress [player]`: View scroll progress.
* `/scroll reset <player> [ability]`: Reset a player's progress for a specific ability.
* `/scroll resetearlygame <player>`: Reset early game rewards for a player.
* `/scroll resetprogress <player>`: Reset all scroll progress for a player.
* `/scroll reload`: Reload the plugin configuration.
* `/scroll page <number>`: Navigate through paginated progress lists.

---

## 📥 Installation

1. **Download** the latest release from the [GitHub Releases](https://github.com/CozmycDev/ProjectKorraScrolls/releases) page.
2. **Place** the `.jar` file into your server's `plugins` directory.
3. **Restart** your server to generate the default configuration files.
4. **Configure** the plugin settings to suit your server's needs.

---

## 🤝 Compatibility

* **ProjectKorra**: 1.12.0
* **Minecraft Versions**: 1.21.4+

---

## 🛣️ Roadmap

The ProjectKorraScrolls plugin is continuously evolving to enhance the bending experience on your server. Here's a glimpse into our upcoming plans:

### 🔄 Enhanced Compatibility with Core

* **Support for All Abilities**: We are still working to ensure full compatibility with all Core abilities. Very few abilities, like AirAgility, aren't compatible, yet.

### 🛠️ Overhaul of the Events System

* **Granular Control over Scroll Generation**: We're planning a comprehensive overhaul of the current events system to provide server owners with more precise control over scroll generation. This includes:

    * **Customizable Drop Conditions**: Define specific conditions under which scrolls can drop, such as time of day, biome, or player level.
    * **More Event-Based Triggers**: Introduce new event triggers for scroll generation, allowing for more dynamic and context-sensitive scroll drops.

Stay tuned for updates as we continue to develop and refine these features.

---
