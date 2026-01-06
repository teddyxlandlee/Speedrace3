package xland.mcplugin.games25.speedrace3;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

public final class GameInstance {
    private final int x;
    private final int z;
    private final double radius;    // Chebyshev radius
    private final transient int radiusAsInt;
    private final SequencedSet<OfflinePlayer> arrivedPlayers = new LinkedHashSet<>();

    public GameInstance(int x, int z, double radius) {
        this.x = x;
        this.z = z;
        this.radius = radius;
        this.radiusAsInt = Math.round(radius) > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) Math.round(radius);
    }

    void tick(final I18n i18n) {
        final ArrayList<Player> arrivingPlayers = new ArrayList<>(2);
        final int prevArrivedPlayerCount = getArrivedPlayerCount();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (checkPlayerArrival(player)) {
                arrivingPlayers.addLast(player);
            }
        }

        if (arrivingPlayers.isEmpty()) return;
        final int arrivedPlayerCount = prevArrivedPlayerCount + 1;
        for (Player arrivingPlayer : arrivingPlayers) {
            Component component = i18n.insertPlayer("info.player-arrived", arrivingPlayer, arrivedPlayerCount);
            Bukkit.broadcast(component);
        }
    }

    public boolean isCoordinationInside(Location location) {
        if (location.getWorld().getEnvironment() != World.Environment.NORMAL) {
            // Only accepts overworld, so far
            return false;
        }
        final double xDiff = Math.abs(location.getX() - x);
        final double zDiff = Math.abs(location.getZ() - z);
        return xDiff <= radius && zDiff <= radius;
    }

    public int getArrivedPlayerCount() {
        return arrivedPlayers.size();
    }

    public List<? extends OfflinePlayer> getArrivedPlayers() {
        return List.copyOf(arrivedPlayers);
    }

    public boolean checkPlayerArrival(Player player) {
        final Location location = player.getLocation();
        if (!isCoordinationInside(location)) {
            return false;
        }
        return arrivedPlayers.add(player);
    }

    void forEachXZPair(boolean edgeOnly, ParticleManager.XZConsumer consumer) {
        if (radiusAsInt <= 0) {
            consumer.accept(x, z);
        }
        if (!edgeOnly) {
            for (int xDiff = -radiusAsInt; xDiff <= radiusAsInt; xDiff++) {
                for (int zDiff = -radiusAsInt; zDiff <= radiusAsInt; zDiff++) {
                    consumer.accept(x + xDiff, z + zDiff);
                }
            }
        } else {
            // only all dots of four sides of the rectangle
            for (int xDiff = -radiusAsInt; xDiff <= radiusAsInt; xDiff++) {     // inclusive
                consumer.accept(x + xDiff, z - radiusAsInt);
                consumer.accept(x + xDiff, z + radiusAsInt);
            }
            for (int zDiff = 1 - radiusAsInt; zDiff < radiusAsInt; zDiff++) {   // exclusive
                consumer.accept(x - radiusAsInt, z + zDiff);
                consumer.accept(x + radiusAsInt, z + zDiff);
            }
        }
    }
}
