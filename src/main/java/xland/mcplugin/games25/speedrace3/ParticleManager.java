package xland.mcplugin.games25.speedrace3;

import org.bukkit.Bukkit;
import org.bukkit.HeightMap;
import org.bukkit.Particle;
import org.bukkit.World;

final class ParticleManager implements Runnable {
    private final boolean isEdgeOnly;
    private final Particle particleType;
    private final int particleCount;
    private final int particleHeight;
    private final boolean particleForceDisplay;

    private final Speedrace3 plugin;

    ParticleManager(Speedrace3 plugin) {
        Config config = plugin.config;
        this.isEdgeOnly = config.getParticlesPosition().isEdgeOnly();
        this.particleType = config.getParticlesType();
        this.particleHeight = config.getParticlesHeight();
        this.particleCount = config.getParticlesCount();
        this.particleForceDisplay = config.isParticlesForceDisplay();
        this.plugin = plugin;
    }

    @Override
    public void run() {
        GameInstance gameInstance = plugin.gameInstance;
        if (gameInstance == null) return;

        Bukkit.getWorlds().stream().filter(world -> world.getEnvironment() == World.Environment.NORMAL)
                .forEach(world -> gameInstance.forEachXZPair(isEdgeOnly, (x, z) -> {
                    // don't try to spawn particles in unloaded chunks
                    if (!world.isChunkLoaded(x >> 4, z >> 4)) return;

                    final int minHeight = world.getHighestBlockYAt(x, z, HeightMap.MOTION_BLOCKING_NO_LEAVES);
                    final int maxHeight = Math.min(minHeight + particleHeight, world.getMaxHeight());

                    final double dx = x + .5, dz = z + .5;
                    for (int height = maxHeight; height > minHeight; height--) {
                        world.spawnParticle(
                                particleType, dx, height + .5, dz,
                                particleCount, .5, .5, .5,
                                0, null, particleForceDisplay
                        );
                    }
                }));
    }

    interface XZConsumer {
        void accept(int x, int z);
    }
}
