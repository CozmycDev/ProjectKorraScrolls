package com.projectkorra.cozmyc.pkscrolls.commands;

import com.projectkorra.cozmyc.pkscrolls.ProjectKorraScrolls;
import com.projectkorra.cozmyc.pkscrolls.models.Scroll;
import com.projectkorra.cozmyc.pkscrolls.utils.ColorUtils;
import com.projectkorra.cozmyc.pkscrolls.utils.ScrollItemFactory;
import com.projectkorra.projectkorra.Element;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ScrollCommand implements CommandExecutor, TabCompleter {

    private final ProjectKorraScrolls plugin;
    private static final Map<UUID, Integer> playerPages = new HashMap<>();

    public ScrollCommand(ProjectKorraScrolls plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give" -> handleGive(sender, args);
            case "progress" -> handleProgress(sender, args);
            case "reset" -> handleReset(sender, args);
            case "reload" -> handleReload(plugin, sender);
            case "resetearlygame" -> handleResetEarlyGame(sender, args);
            case "resetprogress" -> handleResetProgress(sender, args);
            case "page" -> handlePageNavigation(sender, args);
            default -> showHelp(sender);
        }

        return true;
    }

    private boolean handleGive(CommandSender sender, String[] args) {
        ProjectKorraScrolls.getInstance().debugLog("Processing give command from " + sender.getName() + " with args: " + String.join(" ", args));

        if (!sender.hasPermission("pkscrolls.admin")) {
            ProjectKorraScrolls.getInstance().debugLog("Permission denied for " + sender.getName());
            sender.sendMessage(ColorUtils.formatMessage(plugin.getConfigManager().getMessage("noPermission")));
            return true;
        }

        if (args.length < 3) {
            ProjectKorraScrolls.getInstance().debugLog("Invalid arguments length: " + args.length);
            sender.sendMessage(ColorUtils.formatMessage(plugin.getConfigManager().getMessage("commands.give.usage")));
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            ProjectKorraScrolls.getInstance().debugLog("Target player not found: " + args[1]);
            sender.sendMessage(ColorUtils.formatMessage(plugin.getConfigManager().getMessage("invalidPlayer")));
            return true;
        }

        String abilityName = args[2];
        Scroll scroll = plugin.getScrollManager().getScroll(abilityName);
        if (scroll == null) {
            ProjectKorraScrolls.getInstance().debugLog("Invalid ability name: " + abilityName);
            sender.sendMessage(ColorUtils.formatMessage(plugin.getConfigManager().getMessage("invalidAbility")));
            return true;
        }

        int amount = args.length > 3 ? Integer.parseInt(args[3]) : 1;
        ProjectKorraScrolls.getInstance().debugLog("Creating " + amount + " scroll(s) of " + abilityName + " for " + target.getName());
        
        ItemStack scrollItem = ScrollItemFactory.createScroll(scroll);
        scrollItem.setAmount(amount);
        target.getInventory().addItem(scrollItem);

        ProjectKorraScrolls.getInstance().debugLog("Successfully gave scrolls to " + target.getName());
        sender.sendMessage(ColorUtils.formatMessage(plugin.getConfigManager().getMessage("commands.give.success")
            .replace("%amount%", String.valueOf(amount))
            .replace("%scroll%", scroll.getDisplayName())
            .replace("%player%", target.getName())));
        return true;
    }

    private boolean handleProgress(CommandSender sender, String[] args) {
        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ColorUtils.formatMessage("&cThis command can only be used by players"));
                return true;
            }
            handleProgress((Player) sender);
            return true;
        }

        if (!sender.hasPermission("pkscrolls.admin")) {
            sender.sendMessage(ColorUtils.formatMessage(plugin.getConfigManager().getMessage("noPermission")));
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ColorUtils.formatMessage(plugin.getConfigManager().getMessage("invalidPlayer")));
            return true;
        }

        handleProgress(target);
        return true;
    }

    private void handleProgress(Player player) {
        ProjectKorraScrolls.getInstance().debugLog("Processing progress command for " + player.getName());
        
        Map<String, Integer> progress = plugin.getPlayerDataManager().getProgress(player);
        ProjectKorraScrolls.getInstance().debugLog("Found " + progress.size() + " abilities in progress map");
        
        // Filter out any abilities with 0 progress
        progress = progress.entrySet().stream()
            .filter(entry -> entry.getValue() > 0)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        ProjectKorraScrolls.getInstance().debugLog("After filtering 0 progress: " + progress.size() + " abilities");
        
        // Group abilities by element
        Map<Element, List<Map.Entry<String, Integer>>> elementGroups = new LinkedHashMap<>();
        Map<Element, List<Map.Entry<String, Integer>>> subelementGroups = new LinkedHashMap<>();
        
        // Collect all abilities and their elements
        for (Map.Entry<String, Integer> entry : progress.entrySet()) {
            String abilityName = entry.getKey();
            Scroll scroll = plugin.getScrollManager().getScroll(abilityName);
            if (scroll == null) {
                ProjectKorraScrolls.getInstance().debugLog("Scroll not found for ability: " + abilityName);
                plugin.getLogger().warning(plugin.getConfigManager().getMessage("commands.progress.scrollNotFound")
                    .replace("%ability%", abilityName));
                continue;
            }
            
            Element element = scroll.getElement();
            if (element.getName().toLowerCase().contains("sub")) {
                subelementGroups.computeIfAbsent(element, k -> new ArrayList<>()).add(entry);
            } else {
                elementGroups.computeIfAbsent(element, k -> new ArrayList<>()).add(entry);
            }
        }
        
        ProjectKorraScrolls.getInstance().debugLog("Grouped abilities: " + elementGroups.size() + " elements, " + subelementGroups.size() + " subelements");
        
        // Combine all entries for pagination
        List<String> displayLines = new ArrayList<>();
        
        // Add element headers and their abilities
        for (Map.Entry<Element, List<Map.Entry<String, Integer>>> group : elementGroups.entrySet()) {
            if (!group.getValue().isEmpty()) {
                displayLines.add(plugin.getConfigManager().getMessage("commands.progress.elementHeader")
                    .replace("%element%", group.getKey().getName()));
                for (Map.Entry<String, Integer> entry : group.getValue()) {
                    String abilityName = entry.getKey();
                    int count = entry.getValue();
                    Scroll scroll = plugin.getScrollManager().getScroll(abilityName);
                    if (scroll != null) {
                        String prefix = count >= scroll.getUnlockCount() ? 
                            plugin.getConfigManager().getMessage("commands.progress.unlockedPrefix") : 
                            plugin.getConfigManager().getMessage("commands.progress.lockedPrefix");
                        String progressText = count < scroll.getUnlockCount() ? 
                            plugin.getConfigManager().getMessage("commands.progress.progressFormat")
                                .replace("%current%", String.valueOf(count))
                                .replace("%required%", String.valueOf(scroll.getUnlockCount())) : 
                            "";
                        displayLines.add(plugin.getConfigManager().getMessage("commands.progress.abilityFormat")
                            .replace("%prefix%", prefix)
                            .replace("%name%", scroll.getDisplayName())
                            .replace("%progress%", progressText));
                    }
                }
                displayLines.add("");
            }
        }
        
        // Add subelement headers and their abilities
        for (Map.Entry<Element, List<Map.Entry<String, Integer>>> group : subelementGroups.entrySet()) {
            if (!group.getValue().isEmpty()) {
                displayLines.add(plugin.getConfigManager().getMessage("commands.progress.elementHeader")
                    .replace("%element%", group.getKey().getName()));
                for (Map.Entry<String, Integer> entry : group.getValue()) {
                    String abilityName = entry.getKey();
                    int count = entry.getValue();
                    Scroll scroll = plugin.getScrollManager().getScroll(abilityName);
                    if (scroll != null) {
                        String prefix = count >= scroll.getUnlockCount() ? 
                            plugin.getConfigManager().getMessage("commands.progress.unlockedPrefix") : 
                            plugin.getConfigManager().getMessage("commands.progress.lockedPrefix");
                        String progressText = count < scroll.getUnlockCount() ? 
                            plugin.getConfigManager().getMessage("commands.progress.progressFormat")
                                .replace("%current%", String.valueOf(count))
                                .replace("%required%", String.valueOf(scroll.getUnlockCount())) : 
                            "";
                        displayLines.add(plugin.getConfigManager().getMessage("commands.progress.abilityFormat")
                            .replace("%prefix%", prefix)
                            .replace("%name%", scroll.getDisplayName())
                            .replace("%progress%", progressText));
                    }
                }
                displayLines.add("");
            }
        }
        
        // If no progress, show message
        if (displayLines.isEmpty()) {
            player.sendMessage(ColorUtils.formatMessage(plugin.getConfigManager().getMessage("commands.progress.noProgress")));
            return;
        }
        
        // Pagination
        int itemsPerPage = plugin.getConfigManager().getInt("commands.progress.itemsPerPage", 10);
        int totalItems = displayLines.size();
        int totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);
        
        // Get current page from metadata or default to 1
        int currentPage = playerPages.getOrDefault(player.getUniqueId(), 1);
        
        // Calculate start and end indices
        int startIndex = (currentPage - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, totalItems);
        
        // Display header
        player.sendMessage(ColorUtils.addColor(plugin.getConfigManager().getMessage("commands.progress.header")));
        player.sendMessage(ColorUtils.addColor(plugin.getConfigManager().getMessage("commands.progress.title")));
        player.sendMessage(ColorUtils.addColor(plugin.getConfigManager().getMessage("commands.progress.header")));

        // Display items for current page
        for (int i = startIndex; i < endIndex; i++) {
            player.sendMessage(ColorUtils.addColor(displayLines.get(i)));
        }

        if (totalPages > 1) {
            String pageInfo = plugin.getConfigManager().getMessage("commands.progress.pageInfo")
                .replace("%current%", String.valueOf(currentPage))
                .replace("%total%", String.valueOf(totalPages));
            
            // Create the base message with page info
            TextComponent message = new TextComponent(ColorUtils.addColor(pageInfo));
            
            // Add previous button if not on first page
            if (currentPage > 1) {
                String prevButton = plugin.getConfigManager().getMessage("commands.progress.prevButton");
                String prevHover = plugin.getConfigManager().getMessage("commands.progress.prevButtonHover");
                
                TextComponent prevComponent = new TextComponent(ColorUtils.addColor(prevButton));
                prevComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/scrolls page " + (currentPage - 1)));
                prevComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                    new ComponentBuilder(ColorUtils.addColor(prevHover)).create()));
                
                message.addExtra(" ");
                message.addExtra(prevComponent);
            }
            
            // Add next button if not on last page
            if (currentPage < totalPages) {
                String nextButton = plugin.getConfigManager().getMessage("commands.progress.nextButton");
                String nextHover = plugin.getConfigManager().getMessage("commands.progress.nextButtonHover");
                
                TextComponent nextComponent = new TextComponent(ColorUtils.addColor(nextButton));
                nextComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/scrolls page " + (currentPage + 1)));
                nextComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                    new ComponentBuilder(ColorUtils.addColor(nextHover)).create()));
                
                message.addExtra(" ");
                message.addExtra(nextComponent);
            }

            player.spigot().sendMessage(message);
        }

        player.sendMessage(ColorUtils.addColor(plugin.getConfigManager().getMessage("commands.progress.header")));
    }

    private boolean handleReset(CommandSender sender, String[] args) {
        if (!sender.hasPermission("pkscrolls.admin")) {
            sender.sendMessage(ColorUtils.formatMessage(plugin.getConfigManager().getMessage("noPermission")));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ColorUtils.formatMessage(plugin.getConfigManager().getMessage("commands.reset.usage")));
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ColorUtils.formatMessage(plugin.getConfigManager().getMessage("invalidPlayer")));
            return true;
        }

        if (args.length > 2) {
            String abilityName = args[2];
            Scroll scroll = plugin.getScrollManager().getScroll(abilityName);
            if (scroll == null) {
                sender.sendMessage(ColorUtils.formatMessage(plugin.getConfigManager().getMessage("invalidAbility")));
                return true;
            }

            plugin.getPlayerDataManager().resetProgress(target, abilityName);
            sender.sendMessage(ColorUtils.formatMessage(plugin.getConfigManager().getMessage("commands.reset.abilitySuccess")
                .replace("%player%", target.getName())
                .replace("%ability%", scroll.getDisplayName())));
        } else {
            plugin.getPlayerDataManager().resetAllProgress(target);
            sender.sendMessage(ColorUtils.formatMessage(plugin.getConfigManager().getMessage("commands.reset.success")
                .replace("%player%", target.getName())));
        }

        return true;
    }

    public static boolean handleReload(ProjectKorraScrolls plugin, CommandSender sender) {
        if (!sender.hasPermission("pkscrolls.admin")) {
            sender.sendMessage(ColorUtils.formatMessage(plugin.getConfigManager().getMessage("noPermission")));
            return true;
        }

        plugin.getConfigManager().loadConfig();
        plugin.getScrollManager().loadAbilities();
        
        sender.sendMessage(ColorUtils.formatMessage(plugin.getConfigManager().getMessage("commands.reload.success")));
        return true;
    }

    private void handleResetEarlyGame(CommandSender sender, String[] args) {
        if (!sender.hasPermission("pkscrolls.admin")) {
            sender.sendMessage(ColorUtils.formatMessage(plugin.getConfigManager().getMessage("noPermission")));
            return;
        }

        if (args.length != 2) {
            sender.sendMessage(ColorUtils.formatMessage(plugin.getConfigManager().getMessage("commands.resetEarlyGame.usage")));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ColorUtils.formatMessage(plugin.getConfigManager().getMessage("invalidPlayer")));
            return;
        }

        plugin.getPlayerDataManager().resetEarlyGameProgress(target);
        sender.sendMessage(ColorUtils.formatMessage(plugin.getConfigManager().getMessage("commands.resetEarlyGame.success")
            .replace("%player%", target.getName())));
    }

    private void handleResetProgress(CommandSender sender, String[] args) {
        if (!sender.hasPermission("pkscrolls.admin")) {
            sender.sendMessage(ColorUtils.formatMessage(plugin.getConfigManager().getMessage("noPermission")));
            return;
        }

        if (args.length != 2) {
            sender.sendMessage(ColorUtils.formatMessage(plugin.getConfigManager().getMessage("commands.resetProgress.usage")));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ColorUtils.formatMessage(plugin.getConfigManager().getMessage("invalidPlayer")));
            return;
        }

        plugin.getPlayerDataManager().resetPlayer(target);
        sender.sendMessage(ColorUtils.formatMessage(plugin.getConfigManager().getMessage("commands.resetProgress.success")
            .replace("%player%", target.getName())));
    }

    private void showHelp(CommandSender sender) {
        ProjectKorraScrolls.getInstance().debugLog("Showing help menu to " + sender.getName());
        sender.sendMessage(ColorUtils.formatMessage(plugin.getConfigManager().getMessage("commands.help.header")));
        sender.sendMessage(ColorUtils.formatMessage(plugin.getConfigManager().getMessage("commands.help.title")));
        
        if (sender.hasPermission("pkscrolls.admin")) {
            sender.sendMessage(ColorUtils.formatMessage(plugin.getConfigManager().getMessage("commands.help.commands.give")));
            sender.sendMessage(ColorUtils.formatMessage(plugin.getConfigManager().getMessage("commands.help.commands.reset")));
            sender.sendMessage(ColorUtils.formatMessage(plugin.getConfigManager().getMessage("commands.help.commands.resetEarlyGame")));
            sender.sendMessage(ColorUtils.formatMessage(plugin.getConfigManager().getMessage("commands.help.commands.resetProgress")));
            sender.sendMessage(ColorUtils.formatMessage(plugin.getConfigManager().getMessage("commands.help.commands.reload")));
        }
        
        sender.sendMessage(ColorUtils.formatMessage(plugin.getConfigManager().getMessage("commands.help.commands.progress")));
        sender.sendMessage(ColorUtils.formatMessage(plugin.getConfigManager().getMessage("commands.help.commands.page")));
        sender.sendMessage(ColorUtils.formatMessage(plugin.getConfigManager().getMessage("commands.help.footer")));
    }

    private boolean handlePageNavigation(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtils.formatMessage("&cThis command can only be used by players"));
            return true;
        }

        Player player = (Player) sender;
        int currentPage = playerPages.getOrDefault(player.getUniqueId(), 1);
        int newPage = currentPage;

        if (args.length < 2) {
            sender.sendMessage(ColorUtils.formatMessage(plugin.getConfigManager().getMessage("commands.progress.pageUsage")));
            return true;
        }

        String pageArg = args[1].toLowerCase();
        switch (pageArg) {
            case "next" -> newPage++;
            case "prev", "previous" -> newPage--;
            default -> {
                try {
                    newPage = Integer.parseInt(pageArg);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ColorUtils.formatMessage(plugin.getConfigManager().getMessage("commands.progress.invalidPage")));
                    return true;
                }
            }
        }

        if (newPage < 1) {
            sender.sendMessage(ColorUtils.formatMessage(plugin.getConfigManager().getMessage("commands.progress.invalidPage")));
            return true;
        }

        playerPages.put(player.getUniqueId(), newPage);
        handleProgress(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(List.of("give", "progress", "reset", "reload", "resetearlygame", "resetprogress", "page"));
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "give", "progress", "reset", "resetearlygame", "resetprogress" -> {
                    if (sender.hasPermission("pkscrolls.admin")) {
                        completions.addAll(Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .toList());
                    }
                }
                case "page" -> completions.addAll(List.of("next", "prev"));
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            if (sender.hasPermission("pkscrolls.admin")) {
                completions.addAll(plugin.getScrollManager().getAbilityNames());
            }
        }

        return completions.stream()
            .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
            .toList();
    }
}
