package com.projectkorra.cozmyc.pkscrolls.utils;

import com.projectkorra.cozmyc.pkscrolls.ProjectKorraScrolls;
import net.md_5.bungee.api.ChatColor;

import java.util.List;
import java.util.stream.Collectors;

public class ColorUtils {

    public static String addColor(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static List<String> colorizeList(List<String> messages) {
        return messages.stream()
            .map(ColorUtils::addColor)
            .collect(Collectors.toList());
    }

    public static String formatMessage(String message, Object... args) {
        String formatted = ProjectKorraScrolls.getInstance().getConfigManager().getMessage("prefix") + message;
        for (int i = 0; i < args.length; i += 2) {
            if (i + 1 < args.length) {
                String key = String.valueOf(args[i]);
                String value = String.valueOf(args[i + 1]);
                formatted = formatted.replace("%" + key + "%", value)
                        .replace("{" + key + "}", value);
            }
        }
        return addColor(formatted);
    }
}
