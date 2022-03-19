package com.bstudio.smartchat.smartchat;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinQuitEvent implements Listener {

    private final SmartChat plugin;

    public PlayerJoinQuitEvent(SmartChat smartChat) {
        plugin = smartChat;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String joinFormat = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("join-message", "&7[&a+&7] &f{player} has joined the game."));
        joinFormat = joinFormat.replace("{player}", event.getPlayer().getDisplayName());

        event.setJoinMessage(joinFormat);
    }

    public void onPlayerQuit(PlayerQuitEvent event) {
        String quitFormat = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("quit-message", "&7[&c-&7] &f{player} has left the game."));
        quitFormat = quitFormat.replace("{player}", event.getPlayer().getDisplayName());

        event.setQuitMessage(quitFormat);
    }
}
