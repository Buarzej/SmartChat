package com.bstudio.smartchat.smartchat;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class SmartChat extends JavaPlugin {

    private static final int CONFIG_VERSION = 2;
    public Translations messages;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        updateConfig();

        messages = new Translations(this);
        messages.updateLocale(this.getConfig().getString("locale", "en"));

        if (this.getConfig().getBoolean("use-json-chat-format")) {
            this.getServer().getPluginManager().registerEvents(new JSONChatEvent(this, messages), this);
        } else {
            this.getServer().getPluginManager().registerEvents(new ChatEvent(this, messages), this);
        }

        this.getServer().getPluginManager().registerEvents(new PlayerJoinQuitEvent(this), this);

        this.getCommand("chat").setExecutor(new ChatCommandExecutor(this, messages));
        this.getCommand("adminchat").setExecutor(new ChatCommandExecutor(this, messages));
    }

    @Override
    public void onDisable() {
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        if (messages != null) {
            messages.updateLocale(this.getConfig().getString("locale"));
        }
    }

    // Config updater
    private void updateConfig() {
        if (this.getConfig().getInt("config-version") < CONFIG_VERSION) {
            getLogger().info("Updating config to new version...");
            File configFile = new File(this.getDataFolder(), "config.yml");
            File deprecatedConfigFile = new File(this.getDataFolder(), "config_old.yml");
            if (configFile.renameTo(deprecatedConfigFile)) {
                this.saveDefaultConfig();
                getLogger().info("Config updated, you have to configure the plugin again!");
            } else {
                getLogger().info("Updating config failed. Please remove config.yml file from plugin\'s directory and reload the server.");
            }
        } else {
            getLogger().info("Config file is up to date.");
        }
    }

    // Turning chat on/off
    private boolean chatOn = true;

    boolean isChatOn() {
        return chatOn;
    }

    void setChatOn(boolean setTo) {
        chatOn = setTo;
    }
}
