package xland.mcplugin.games25.speedrace3;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

public final class Speedrace3 extends JavaPlugin {
    Config config;
    final I18n i18n = new I18n();

    @Nullable GameInstance gameInstance;

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.saveDefaultConfig();
        this.config = new Config(this.getConfig());
        this.i18n.loadFrom(this, this.getConfig().getString("lang", "en"));

        this.registerCommands();

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (gameInstance != null) {
                gameInstance.tick(i18n);
            }
        }, 1, 2);

        if (this.config.isParticlesEnabled()) {
            Bukkit.getScheduler().runTaskTimer(this, new ParticleManager(this), 10, config.getParticlesInterval());
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private void registerCommands() {
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, new TheCommands(this));
    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
