package com.projectkorra.cozmyc.pkscrolls.hooks;

import com.projectkorra.cozmyc.pkscrolls.ProjectKorraScrolls;
import com.projectkorra.cozmyc.pkscrolls.models.Scroll;
import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
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
            return Optional.of(false);
        }
        if (bPlayer == null) {
            return Optional.of(false);
        }

        Player player = bPlayer.getPlayer();
        String abilityName = ability.getName();
        Scroll scroll = plugin.getScrollManager().getScroll(abilityName);
        if (scroll == null) {
            return Optional.empty();
        }
        if (!ability.isEnabled()) {
            return Optional.of(false);
        }
        if (scroll.permissionCanBypassBindHooks() && ability.isHiddenAbility() && player.hasPermission("bending.ability." + ability.getName().toLowerCase())) {
            return Optional.of(true);
        }
        if (scroll.permissionCanBypassBindHooks() && player.hasPermission("bending.ability." + ability.getName().toLowerCase())) {
            return Optional.of(true);
        }
        if (!bPlayer.hasElement(scroll.getElement())) {
            return Optional.of(false);
        }
        
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        if (!scroll.permissionCanBypassBindHooks() && !pdc.has(plugin.getNamespacedKey("unlocked_" + abilityName), PersistentDataType.BYTE)) {
            return Optional.of(false);
        }

        return Optional.of(true);
    }

    @Override
    public Optional<Boolean> canBend(BendingPlayer bPlayer, CoreAbility ability, boolean isCheckingBind, boolean isCheckingCooldown) {
        // removed since passthrough checks these anyways
//        if (ability == null) {
//            return Optional.of(false);
//        }
//        if (bPlayer == null) {
//            return Optional.of(false);
//        }
//        if (!ability.isEnabled()) {
//            return Optional.of(false);
//        }
//        if (!bPlayer.isToggled()) {
//            return Optional.of(false);
//        }

        Player player = bPlayer.getPlayer();
        String abilityName = ability.getName();
        Scroll scroll = plugin.getScrollManager().getScroll(abilityName);

        if (scroll == null) {
            return Optional.empty();
        }

        if (!bPlayer.isElementToggled(scroll.getElement())) {
            return Optional.of(false);
        }
        if (!bPlayer.isPassiveToggled(scroll.getElement()) && ability instanceof PassiveAbility) {
            return Optional.of(false);
        }
        if (!bPlayer.isToggledPassives() && ability instanceof PassiveAbility) {
            return Optional.of(false);
        }

        if (scroll.permissionCanBypassBindHooks() && player.hasPermission("bending.ability." + ability.getName().toLowerCase())) {
            return Optional.empty();
        }

        if (!bPlayer.hasElement(scroll.getElement())) {
            return Optional.of(false);
        }

        PersistentDataContainer pdc = player.getPersistentDataContainer();
        if (!scroll.permissionCanBypassBindHooks() && !pdc.has(plugin.getNamespacedKey("unlocked_" + abilityName), PersistentDataType.BYTE)) {
            return Optional.of(false);
        }

        return Optional.empty();
    }
}
