package com.bstudio.smartchat.smartchat;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class JSONChatEvent implements Listener {

    private final SmartChat plugin;
    private final Translations messages;

    public JSONChatEvent(SmartChat smartChat, Translations translations) {
        plugin = smartChat;
        messages = translations;
    }

    private ComponentBuilder buildJSONMessage(Player p, String originalMessage) {
        ComponentBuilder message = new ComponentBuilder("");
        ConfigurationSection jsonFormat = plugin.getConfig().getConfigurationSection("json-chat-format");
        Set<String> parts = jsonFormat.getKeys(false);
        Iterator<String> it = parts.iterator();

        // Iterowanie się po wszystkich częściach formatu JSON
        while (it.hasNext()) {
            String chatPartName = it.next();
            ConfigurationSection chatPart = jsonFormat.getConfigurationSection(chatPartName);
            String partMessage = chatPart.getString("message");

            // Dzielenie color code'ów na substringi
            int beginning = 0; // początek substringa
            for (int i = 0; i < partMessage.length(); i++) {
                if (partMessage.charAt(i) == '&' || (i + 1) == partMessage.length()) {
                    if (i + 1 == partMessage.length()) {
                        i++;
                    }
                    String toAppend = partMessage.substring(beginning, i);
                    toAppend = toAppend.replace("{player}", p.getName());
                    toAppend = toAppend.replace("{message}", originalMessage);
                    message.appendLegacy(ChatColor.translateAlternateColorCodes('&', toAppend));

                    // Dodawanie akcji najechania i kliknięcia do substringa
                    if (chatPart.getString("hover-action") != null) {
                        String hoverValue;
                        if (chatPart.getStringList("hover-value").isEmpty()) {
                            hoverValue = chatPart.getString("hover-value");
                        } else {
                            hoverValue = String.join("\n", chatPart.getStringList("hover-value"));
                        }
                        hoverValue = hoverValue.replace("{player}", p.getName());
                        hoverValue = hoverValue.replace("{message}", originalMessage);
                        message.event(new HoverEvent(HoverEvent.Action.valueOf(chatPart.getString("hover-action")), TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', hoverValue))));
                    }
                    if (chatPart.getString("click-action") != null) {
                        String clickValue = chatPart.getString("click-value");
                        clickValue = clickValue.replace("{player}", p.getName());
                        clickValue = clickValue.replace("{message}", originalMessage);
                        message.event(new ClickEvent(ClickEvent.Action.valueOf(chatPart.getString("click-action")), clickValue));
                    }
                    beginning = i; // przesunięcie początku kolejnego substringa
                }
            }
            if (it.hasNext()) { // dodawanie spacji między częściami formatu JSON
                message.append(" ");
                message.reset();
            }
        }
        return message;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {

        Player p = event.getPlayer();
        String originalMessage;
        ArrayList<String> mentionedPlayers = new ArrayList<String>();

        event.getRecipients().clear();

        if (!plugin.isChatOn() && !p.hasPermission("smartchat.mute.bypass")) {
            event.setCancelled(true);
            p.sendMessage(messages.translate("cantSpeak"));
        }

        if ((!plugin.getConfig().getBoolean("enable-color-permissions", false)) || p.hasPermission("smartchat.color")) {
            originalMessage = ChatColor.translateAlternateColorCodes('&', event.getMessage());
        } else {
            originalMessage = event.getMessage();
        }

        if (plugin.getConfig().getBoolean("enable-location-tag", true) && p.hasPermission("smartchat.location")) {
            originalMessage = originalMessage.replace(":loc:", p.getLocation().getBlockX() + ", " + p.getLocation().getBlockY() + ", " + p.getLocation().getBlockZ());
        }

        if (plugin.getConfig().getBoolean("enable-chat-mentions", true) && p.hasPermission("smartchat.mentions")) {
            String tempMessage = originalMessage.toUpperCase();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (tempMessage.contains(player.getName().toUpperCase()) && !player.hasPermission("smartchat.mentions.ignore")) {
                    mentionedPlayers.add(player.getName());
                    if (plugin.getConfig().getBoolean("chat-mentions-sound", true)) {
                        Sound sound = Sound.valueOf(plugin.getConfig().getString("chat-mentions-sound-type"));
                        float volume = (float) plugin.getConfig().getDouble("chat-mentions-sound-volume");
                        float pitch = (float) plugin.getConfig().getDouble("chat-mentions-sound-pitch");
                        player.playSound(player.getLocation(), sound, volume, pitch);
                    }
                    if (plugin.getConfig().getBoolean("chat-mentions-action-bar", true)) {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(messages.translateNoPrefix("mentionsActionBar", p.getDisplayName())));
                    }
                    player.spigot().sendMessage(buildJSONMessage(p, originalMessage.replaceAll("(?i)" + player.getName(), ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("chat-mentions-format", "&b") + player.getName() + "&r"))).create());
                }
            }
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!mentionedPlayers.contains(player.getName())) {
                player.spigot().sendMessage(buildJSONMessage(p, originalMessage).create());
            }
        }
    }
}
