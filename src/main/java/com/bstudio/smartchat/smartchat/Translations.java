package com.bstudio.smartchat.smartchat;

import org.bukkit.ChatColor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Translations {

    private final SmartChat plugin;
    private final Locale defaultLocale = Locale.getDefault();
    private Locale currentLocale = defaultLocale;
    private final ResourceBundle defaultBundle;
    private ResourceBundle localeBundle;
    private ResourceBundle customBundle;
    private static final ResourceBundle NULL_BUNDLE = new ResourceBundle() {
        @Override
        protected Object handleGetObject(String s) {
            return null;
        }

        @Override
        public Enumeration<String> getKeys() {
            return null;
        }
    };

    public Translations(SmartChat smartChat) {
        plugin = smartChat;
        defaultBundle = ResourceBundle.getBundle("messages", Locale.ENGLISH);
        localeBundle = defaultBundle;
        customBundle = NULL_BUNDLE;
    }

    public void updateLocale(String locale) {
        if (locale != null && !locale.isEmpty()) {
            final String[] parts = locale.split("[_.]");
            if (parts.length == 1) {
                currentLocale = new Locale(parts[0]);
            }
            if (parts.length == 2) {
                currentLocale = new Locale(parts[0], parts[1]);
            }
            if (parts.length == 3) {
                currentLocale = new Locale(parts[0], parts[1], parts[2]);
            }
        }
        ResourceBundle.clearCache();
        plugin.getLogger().info(String.format("Using locale %s.", currentLocale.toString()));

        try {
            localeBundle = ResourceBundle.getBundle("messages", currentLocale);
        } catch (MissingResourceException ex) {
            localeBundle = NULL_BUNDLE;
        }

        try {
            customBundle = ResourceBundle.getBundle("messages", currentLocale, new FileResClassLoader(Translations.class.getClassLoader(), plugin));
        } catch (MissingResourceException ex) {
            customBundle = NULL_BUNDLE;
        }
    }

    public String translate(String string) {
        try {
            try {
                return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix") + customBundle.getString(string));
            } catch (MissingResourceException ex) {
                return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix") + localeBundle.getString(string));
            }
        } catch (MissingResourceException ex) {
            plugin.getLogger().info(String.format("Missing translation \"%s\" in translation file %s.", ex.getKey(), localeBundle.getLocale().toString()));
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix") + defaultBundle.getString(string));
        }
    }

    public String translate(String string, Object... arguments) {
        try {
            return MessageFormat.format(translate(string), arguments);
        } catch (IllegalArgumentException ex) {
            plugin.getLogger().info(String.format("Invalid message format \"%s\": %s", string, ex.getMessage()));
            return MessageFormat.format(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix") + defaultBundle.getString(string)), arguments);
        }
    }

    public String translateNoPrefix(String string) {
        try {
            try {
                return ChatColor.translateAlternateColorCodes('&', customBundle.getString(string));
            } catch (MissingResourceException ex) {
                return ChatColor.translateAlternateColorCodes('&', localeBundle.getString(string));
            }
        } catch (MissingResourceException ex) {
            plugin.getLogger().info(String.format("Missing translation \"%s\" in translation file %s.", ex.getKey(), localeBundle.getLocale().toString()));
            return ChatColor.translateAlternateColorCodes('&', defaultBundle.getString(string));
        }
    }

    public String translateNoPrefix(String string, Object... arguments) {
        try {
            return MessageFormat.format(translateNoPrefix(string), arguments);
        } catch (IllegalArgumentException ex) {
            plugin.getLogger().info(String.format("Invalid message format \"%s\": %s", string, ex.getMessage()));
            return MessageFormat.format(ChatColor.translateAlternateColorCodes('&', defaultBundle.getString(string)), arguments);
        }
    }

    private static class FileResClassLoader extends ClassLoader {
        private final transient File dataFolder;

        FileResClassLoader(final ClassLoader classLoader, final SmartChat smartChat) {
            super(classLoader);
            this.dataFolder = smartChat.getDataFolder();
        }

        @Override
        public URL getResource(final String string) {
            final File file = new File(dataFolder, string);
            if (file.exists()) {
                try {
                    return file.toURI().toURL();
                } catch (MalformedURLException ex) {
                }
            }
            return null;
        }

        @Override
        public InputStream getResourceAsStream(final String string) {
            final File file = new File(dataFolder, string);
            if (file.exists()) {
                try {
                    return new FileInputStream(file);
                } catch (FileNotFoundException ex) {
                }
            }
            return null;
        }
    }
}
