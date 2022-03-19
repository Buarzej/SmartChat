package com.bstudio.smartchat.smartchat;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.Set;

public class ChatCommandExecutor implements CommandExecutor {

    private final SmartChat plugin;
    private final Translations messages;

    public ChatCommandExecutor(SmartChat smartChat, Translations translations) {
        plugin = smartChat;
        messages = translations;
    }

    @Override
    public boolean onCommand(CommandSender player, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("chat")) {
            if (args.length < 1) {
                player.sendMessage(messages.translate("correctUsage", "/" + alias + " <on/off/clear/send/reload>."));
            } else if (args[0].equalsIgnoreCase("on")) {
                if (player.hasPermission("smartchat.mute")) {
                    if (plugin.isChatOn()) {
                        player.sendMessage(messages.translate("chatIsOn"));
                    } else {
                        plugin.setChatOn(true);
                        if (plugin.getConfig().getBoolean("announce-chat-on", true)) {
                            Bukkit.broadcastMessage(messages.translate("chatTurnedOnAnnouncement"));
                        } else {
                            player.sendMessage(messages.translate("chatTurnedOn"));
                        }
                    }
                } else {
                    player.sendMessage(messages.translate("noCommandPermission"));
                }
            } else if (args[0].equalsIgnoreCase("off")) {
                if (player.hasPermission("smartchat.mute")) {
                    if (!plugin.isChatOn()) {
                        player.sendMessage(messages.translate("chatIsOff"));
                    } else {
                        plugin.setChatOn(false);
                        if (plugin.getConfig().getBoolean("announce-chat-off", true)) {
                            Bukkit.broadcastMessage(messages.translate("chatTurnedOffAnnouncement"));
                        } else {
                            player.sendMessage(messages.translate("chatTurnedOff"));
                        }
                    }
                } else {
                    player.sendMessage(messages.translate("noCommandPermission"));
                }
            } else if (args[0].equalsIgnoreCase("clear")) {
                if (player.hasPermission("smartchat.clear")) {
                    for (int i = 0; i < 99; i++) {
                        Bukkit.broadcastMessage("");
                    }
                    Bukkit.broadcastMessage(messages.translate("chatCleared"));
                } else {
                    player.sendMessage(messages.translate("noCommandPermission"));
                }
            } else if (args[0].equalsIgnoreCase("send")) {
                if (player.hasPermission("smartchat.send")) {
                    if (args.length < 2) {
                        player.sendMessage(messages.translate("chatSendNoMessage"));
                    } else {
                        StringBuilder message = new StringBuilder();
                        for (int i = 1; i < args.length; i++) {
                            message.append(args[i]);
                            if (i + 1 != args.length) {
                                message.append(" ");
                            }
                        }
                        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message.toString()));
                    }
                } else {
                    player.sendMessage(messages.translate("noCommandPermission"));
                }
            } else if (args[0].equalsIgnoreCase("reload")) {
                if (player.hasPermission("smartchat.reload")) {
                    plugin.reloadConfig();
                    player.sendMessage(messages.translate("pluginReloaded"));
                } else {
                    player.sendMessage(messages.translate("noCommandPermission"));
                }
            } else {
                player.sendMessage(messages.translate("correctUsage", "/" + alias + " <on/off/clear/send/reload>."));
            }
            return true;
        } else if (command.getName().equalsIgnoreCase("adminchat")) {
            if (player.hasPermission("smartchat.admin")) {
                if (args.length < 1) {
                    player.sendMessage(messages.translate("chatAdminNoMessage"));
                } else {
                    // Budowanie wiadomości z argumentu komendy /adminchat
                    StringBuilder originalMessage = new StringBuilder();
                    for (int i = 0; i < args.length; i++) {
                        originalMessage.append(args[i]);
                        if (i + 1 != args.length) {
                            originalMessage.append(" ");
                        }
                    }

                    // Wykrywanie formatu admin chatu
                    if (plugin.getConfig().getBoolean("use-json-admin-chat-format")) {
                        ComponentBuilder message = new ComponentBuilder("");
                        ConfigurationSection jsonFormat = plugin.getConfig().getConfigurationSection("json-admin-chat-format");
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
                                    toAppend = toAppend.replace("{player}", player.getName());
                                    toAppend = toAppend.replace("{message}", originalMessage.toString());
                                    message.appendLegacy(ChatColor.translateAlternateColorCodes('&', toAppend));

                                    // Dodawanie akcji najechania i kliknięcia do substringa
                                    if (chatPart.getString("hover-action") != null) {
                                        String hoverValue;
                                        if (chatPart.getStringList("hover-value").isEmpty()) {
                                            hoverValue = chatPart.getString("hover-value");
                                        } else {
                                            hoverValue = String.join("\n", chatPart.getStringList("hover-value"));
                                        }
                                        hoverValue = hoverValue.replace("{player}", player.getName());
                                        hoverValue = hoverValue.replace("{message}", originalMessage.toString());
                                        message.event(new HoverEvent(HoverEvent.Action.valueOf(chatPart.getString("hover-action")), TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', hoverValue))));
                                    }
                                    if (chatPart.getString("click-action") != null) {
                                        String clickValue = chatPart.getString("click-value");
                                        clickValue = clickValue.replace("{player}", player.getName());
                                        clickValue = clickValue.replace("{message}", originalMessage.toString());
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
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (p.hasPermission("smartchat.admin")) {
                                p.spigot().sendMessage(message.create());
                            }
                        }
                    } else {
                        String format = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("admin-chat-format", "&c[AdminChat] &f{player}&7: &f{message}"));
                        format = format.replace("{player}", player.getName());
                        format = format.replace("{message}", ChatColor.translateAlternateColorCodes('&', originalMessage.toString()));
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (p.hasPermission("smartchat.admin")) {
                                p.sendMessage(format);
                            }
                        }
                    }
                }
            } else {
                player.sendMessage(messages.translate("noCommandPermission"));
            }
            return true;
        }
        return false;
    }
}
