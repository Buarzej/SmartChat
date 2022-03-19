package com.bstudio.smartchat.smartchat;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Iterator;

public class ChatEvent implements Listener {

    private final SmartChat plugin;
    private final Translations messages;

    public ChatEvent(SmartChat smartChat, Translations translations) {
        plugin = smartChat;
        messages = translations;
    }

    @EventHandler
    public void chatFormat(AsyncPlayerChatEvent event) {
        Player p = event.getPlayer();
        String msg;
        String tempMsg;

        if (!plugin.isChatOn() && !p.hasPermission("smartchat.mute.bypass")) {
            event.setCancelled(true);
            p.sendMessage(messages.translate("cantSpeak"));
        }

        String format = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("chat-format", "{player}&7: &f{message}"));
        format = format.replace("{player}", p.getDisplayName());

        if ((!plugin.getConfig().getBoolean("enable-color-permissions", false)) || p.hasPermission("smartchat.color")) {
            msg = ChatColor.translateAlternateColorCodes('&', event.getMessage());
        } else {
            msg = event.getMessage();
        }

        if (plugin.getConfig().getBoolean("enable-location-tag", true) && p.hasPermission("smartchat.location")) {
            msg = msg.replace(":loc:", p.getLocation().getBlockX() + ", " + p.getLocation().getBlockY() + ", " + p.getLocation().getBlockZ());
        }

        if (plugin.getConfig().getBoolean("enable-chat-mentions", true) && p.hasPermission("smartchat.mentions")) {
            tempMsg = msg.toUpperCase();
            for (Iterator<Player> it = event.getRecipients().iterator(); it.hasNext(); ) {
                Player mentionedPlayer = it.next();
                if (tempMsg.contains(mentionedPlayer.getName().toUpperCase()) && !mentionedPlayer.hasPermission("smartchat.mentions.ignore")) {
                    it.remove();
                    mentionedPlayer.sendMessage(format.replace("{message}", msg.replaceAll("(?i)" + mentionedPlayer.getName(), ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("chat-mentions-format", "&b") + mentionedPlayer.getName() + "&r"))));
                    if (plugin.getConfig().getBoolean("chat-mentions-sound", true)) {
                        Sound sound = Sound.valueOf(plugin.getConfig().getString("chat-mentions-sound-type"));
                        float volume = (float) plugin.getConfig().getDouble("chat-mentions-sound-volume");
                        float pitch = (float) plugin.getConfig().getDouble("chat-mentions-sound-pitch");
                        mentionedPlayer.playSound(mentionedPlayer.getLocation(), sound, volume, pitch);
                    }
                    if (plugin.getConfig().getBoolean("chat-mentions-action-bar", true)) {
                        mentionedPlayer.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(messages.translateNoPrefix("mentionsActionBar", p.getDisplayName())));
                    }
                }
            }
        }

        format = format.replace("{message}", msg);
        event.setFormat(format);
    }
}
