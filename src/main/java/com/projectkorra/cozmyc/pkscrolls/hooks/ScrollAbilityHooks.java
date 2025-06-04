package com.projectkorra.cozmyc.pkscrolls.hooks;

import com.projectkorra.cozmyc.pkscrolls.ProjectKorraScrolls;
import com.projectkorra.cozmyc.pkscrolls.models.Scroll;
import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.hooks.CanBendHook;
import com.projectkorra.projectkorra.hooks.CanBindHook;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Optional;

public class ScrollAbilityHooks implements CanBindHook, CanBendHook {

    private final ProjectKorraScrolls plugin;

    public ScrollAbilityHooks(ProjectKorraScrolls plugin) {
        this.plugin = plugin;
    }

    public void registerHooks() {
        BendingPlayer.registerCanBindHook(plugin, this);
        BendingPlayer.registerCanBendHook(plugin, this);
    }

    @Override
    public Optional<Boolean> canBind(BendingPlayer bPlayer, CoreAbility ability) {
        if (ability == null) {
            return Optional.empty();
        }
        if (bPlayer == null) {
            return Optional.empty();
        }
        if (ability.isHiddenAbility() || !ability.isEnabled()) {
            return Optional.of(false);
        }

        Player player = bPlayer.getPlayer();
        String abilityName = ability.getName();
        Scroll scroll = plugin.getScrollManager().getScroll(abilityName);
        
        // If no scroll exists for this ability, allow binding
        if (scroll == null) {
            return Optional.empty();
        }
        
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        // todo config toggle to allow multiple elements??
        if (!bPlayer.hasElement(scroll.getElement())) {
            return Optional.of(false);
        }
        if (!pdc.has(plugin.getNamespacedKey("unlocked_" + abilityName), PersistentDataType.BYTE)) {
            return Optional.of(false);
        }
        return Optional.of(true);
    }

    @Override
    public Optional<Boolean> canBend(BendingPlayer bPlayer, CoreAbility ability, boolean ignoreCooldown, boolean ignoreBinds) {
        if (ability == null) {
            return Optional.empty();
        }
        if (bPlayer == null) {
            return Optional.empty();
        }
        if (!ability.isEnabled()) {
            return Optional.of(false);
        }
        if (ability.isHiddenAbility()) {
            return Optional.of(true);
        }

        Player player = bPlayer.getPlayer();
        String abilityName = ability.getName();
        Scroll scroll = plugin.getScrollManager().getScroll(abilityName);
        
        // If no scroll exists for this ability, allow bending
        if (scroll == null) {
            return Optional.of(true);
        }
        
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        if (!bPlayer.hasElement(scroll.getElement())) {
            return Optional.of(false);
        }
        if (!pdc.has(plugin.getNamespacedKey("unlocked_" + abilityName), PersistentDataType.BYTE)) {
            return Optional.of(false);
        }
        return Optional.of(true);
    }
}
