package xland.mcplugin.games25.speedrace3;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.TagPattern;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.util.Objects;

public final class I18n {
    private @NotNull Configuration config = new MemoryConfiguration();
    private @Nullable Configuration defaultConfig;
    public static final String DEFAULT_LANG = "en";

    public void loadFrom(Reader r1, @Nullable Reader r2) {
        try (r1; r2) {
            config = YamlConfiguration.loadConfiguration(r1);
            if (r2 != null)
                defaultConfig = YamlConfiguration.loadConfiguration(r2);
        } catch (Exception ignore) {
        }
    }

    void loadFrom(@NotNull Plugin plugin, @NotNull String lang) {
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(lang, "lang");
        Reader r1, r2;
        if (DEFAULT_LANG.equals(lang)) {
            r2 = null;
        } else {
            r2 = saveAndGet(plugin, DEFAULT_LANG);
        }
        r1 = saveAndGet(plugin, lang);
        this.loadFrom(r1, r2);
    }

    private static Reader saveAndGet(Plugin plugin, String lang) {
        String resourcePath = "lang/" + lang + ".yml";
        plugin.saveResource(resourcePath, false);
        try {
            return Files.newBufferedReader(plugin.getDataPath().resolve(resourcePath));
        } catch (IOException e) {
            plugin.getSLF4JLogger().error("Failed to load language file {}", resourcePath, e);
            return Reader.nullReader();
        }
    }

    public @NotNull String getRaw(@NotNull String key) {
        Objects.requireNonNull(key, "key");

        String s = config.getString(key);
        if (s == null && defaultConfig != null) {
            s = defaultConfig.getString(key);
        }
        if (s == null) {
            return key;
        }

        return s;
    }

    public @NotNull String getRaw(@NotNull String key, Object @NotNull... args) {
        Objects.requireNonNull(args, "args");

        if (args.length == 0) return getRaw(key);
        return String.format(getRaw(key), args);
    }

    public @NotNull Component get(@NotNull String key) {
        return MiniMessage.miniMessage().deserialize(getRaw(key));
    }

    public @NotNull Component get(@NotNull String key, Object @NotNull... args) {
        return MiniMessage.miniMessage().deserialize(getRaw(key, args));
    }

    public @NotNull Component insertPlayer(@NotNull String key, @NotNull Player player, Object @NotNull... args) {
        Objects.requireNonNull(player, "player");

        return insertComponent(key, "player", player.displayName(), args);
    }

    public @NotNull Component insertComponent(@NotNull String key, @NotNull @TagPattern String placeholder, @NotNull Component component, Object @NotNull... args) {
        Objects.requireNonNull(placeholder, "placeholder");
        Objects.requireNonNull(component, "component");

        String s = getRaw(key, args);
        return MiniMessage.miniMessage().deserialize(s, Placeholder.component(placeholder, component));
    }

}
