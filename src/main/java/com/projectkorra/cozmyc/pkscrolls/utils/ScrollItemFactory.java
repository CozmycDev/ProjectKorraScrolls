package com.projectkorra.cozmyc.pkscrolls.utils;

import com.projectkorra.cozmyc.pkscrolls.ProjectKorraScrolls;
import com.projectkorra.cozmyc.pkscrolls.models.Scroll;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class ScrollItemFactory {

    private static final String ABILITY_KEY = "scroll_ability";

    public static ItemStack createScroll(Scroll scroll) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            String elementColor = ProjectKorraScrolls.getInstance().getConfigManager().getConfig()
                    .getString("elementColors." + scroll.getElement().getName(), "&f");

            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                    elementColor + scroll.getDisplayName() + " Scroll"));

            List<String> lore = List.of(scroll.getDescription());
            meta.setLore(ColorUtils.colorizeList(lore));
            meta.setCustomModelData(scroll.getModelData());

            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(ProjectKorraScrolls.getInstance().getNamespacedKey(ABILITY_KEY), PersistentDataType.STRING, scroll.getAbilityName());

            item.setItemMeta(meta);
        }

        return item;
    }

    public static boolean isScroll(ItemStack item) {
        if (item == null || item.getType() != Material.PAPER) return false;
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.has(ProjectKorraScrolls.getInstance().getNamespacedKey(ABILITY_KEY), PersistentDataType.STRING);
    }

    public static String getScrollAbility(ItemStack item) {
        if (!isScroll(item)) return null;
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.get(ProjectKorraScrolls.getInstance().getNamespacedKey(ABILITY_KEY), PersistentDataType.STRING);
    }
}
