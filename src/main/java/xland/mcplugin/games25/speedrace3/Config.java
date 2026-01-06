package xland.mcplugin.games25.speedrace3;

import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

final class Config {
    private final double defaultOffsetRadius;
    private final boolean particlesEnabled;
    private final Particle particlesType;
    private final int particlesCount;
    private final ParticlesPosition particlesPosition;
    private final boolean particlesForceDisplay;
//    private final boolean particlesOnlyAboveGround;
    private final int particlesHeight;
    private final int particlesInterval;

    public Config(org.bukkit.configuration.Configuration configuration) {
        this.defaultOffsetRadius = Math.max(1.0, configuration.getDouble("default-offset-radius", 8.0));
        this.particlesEnabled = configuration.getBoolean("particles.enabled", false);
        this.particlesInterval = Math.max(10, configuration.getInt("particles.interval", 60));

        var particlesTypeString = configuration.getString("particles.type", "minecraft:end_rod");
        this.particlesType = getParticleType(particlesTypeString);

        this.particlesCount = Math.max(1, configuration.getInt("particles.count", 3));
        this.particlesPosition = ParticlesPosition.fromString(configuration.getString("particles.position", "edge"));

        this.particlesForceDisplay = configuration.getBoolean("particles.force", true);
        this.particlesHeight = Math.max(1, configuration.getInt("particles.height", 10));
    }

    private static Particle getParticleType(String s) {
        NamespacedKey key = NamespacedKey.fromString(s);
        Particle p;
        return key == null || (p = Registry.PARTICLE_TYPE.get(key)) == null ? Particle.END_ROD : p;
    }

    public double getDefaultOffsetRadius() {
        return defaultOffsetRadius;
    }

    public boolean isParticlesEnabled() {
        return particlesEnabled;
    }

    public Particle getParticlesType() {
        return particlesType;
    }

    public int getParticlesCount() {
        return particlesCount;
    }

    public ParticlesPosition getParticlesPosition() {
        return particlesPosition;
    }

    public boolean isParticlesForceDisplay() {
        return particlesForceDisplay;
    }

    public int getParticlesInterval() {
        return particlesInterval;
    }

    public int getParticlesHeight() {
        return particlesHeight;
    }

    public enum ParticlesPosition implements StringRepresentable {
        EDGE("edge"),
        ALL("all"),
        ;
        private final String asString;

        ParticlesPosition(String asString) {
            this.asString = asString;
        }

        private static final Map<String, ParticlesPosition> BY_STRING = StringRepresentable.byString(values());

        public String getAsString() {
            return asString;
        }

        public static ParticlesPosition fromString(String asString) {
            return BY_STRING.getOrDefault(asString, EDGE);
        }

        public boolean isEdgeOnly() {
            return this == EDGE;
        }
    }

    private interface StringRepresentable {
        String getAsString();

        static <E extends Enum<E> & StringRepresentable> Map<String, E> byString(E[] values) {
            return Arrays.stream(values)
                    .collect(Collectors.toMap(StringRepresentable::getAsString, Function.identity()));
        }
    }
}
