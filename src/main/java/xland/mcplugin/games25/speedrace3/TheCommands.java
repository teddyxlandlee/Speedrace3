package xland.mcplugin.games25.speedrace3;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.handler.LifecycleEventHandler;
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

@SuppressWarnings("UnstableApiUsage")
final class TheCommands implements LifecycleEventHandler<ReloadableRegistrarEvent<Commands>> {
    private final Speedrace3 plugin;

    TheCommands(Speedrace3 plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run(ReloadableRegistrarEvent<Commands> event) {
        event.registrar().register(
                Commands.literal("speedrace3")
                        .then(Commands.literal("start")
                                .requires(requirePermission("speedrace3.admin"))
                                .then(Commands.argument("x", IntegerArgumentType.integer())
                                        .then(Commands.argument("z", IntegerArgumentType.integer())
                                                .executes(ctx -> start(ctx, false))
                                                .then(Commands.argument("radius", DoubleArgumentType.doubleArg(1.0))
                                                        .executes(ctx -> start(ctx, true))
                                                )
                                        )
                                )
                        )
                        .then(Commands.literal("stop")
                                .requires(requirePermission("speedrace3.admin"))
                                .executes(this::stop)
                        )
                        .then(Commands.literal("query")
                                .requires(requirePermission("speedrace3.query"))
                                .executes(this::query)
                        )
                        .build(),
                List.of("speedrace", "spr")
        );
    }

    private Predicate<CommandSourceStack> requirePermission(String perm) {
        return stack -> stack.getSender().hasPermission(perm);
    }

    private int start(CommandContext<CommandSourceStack> context, boolean captureRadiusFromArgs) {
        if (plugin.gameInstance != null) {
            context.getSource().getSender().sendMessage(plugin.i18n.get("error.game-already-started"));
            return 0;
        }

        final int x = IntegerArgumentType.getInteger(context, "x");
        final int z = IntegerArgumentType.getInteger(context, "z");
        final double radius = captureRadiusFromArgs ? DoubleArgumentType.getDouble(context, "radius") : plugin.config.getDefaultOffsetRadius();

        plugin.gameInstance = new GameInstance(x, z, radius);
        Bukkit.broadcast(plugin.i18n.get("info.game-started", x, z, radius));
        return 1;
    }

    private int stop(CommandContext<CommandSourceStack> context) {
        if (plugin.gameInstance == null) {
            context.getSource().getSender().sendMessage(plugin.i18n.get("error.game-not-started"));
            return 0;
        }
        plugin.gameInstance = null;
        Bukkit.broadcast(plugin.i18n.get("info.game-stopped"));
        return 1;
    }

    private int query(CommandContext<CommandSourceStack> context) {
        if (plugin.gameInstance == null) {
            context.getSource().getSender().sendMessage(plugin.i18n.get("error.game-not-started"));
            return 0;
        }
        final GameInstance gameInstance = plugin.gameInstance;
        final int arrivedPlayerCount = gameInstance.getArrivedPlayerCount();
        if (arrivedPlayerCount == 0) {
            context.getSource().getSender().sendMessage(plugin.i18n.get("info.game-query-no-player"));
        } else {
            context.getSource().getSender().sendMessage(plugin.i18n.insertComponent(
                    "info.game-query",
                    "players", renderPlayers(gameInstance.getArrivedPlayers()),
                    arrivedPlayerCount
            ));
        }
        return 1;
    }

    private static Component renderPlayers(List<? extends OfflinePlayer> players) {
        var components = players.stream()
                .map(p -> {
                    Player player = p.getPlayer();
                    if (player != null) return player.displayName();

                    UUID uuid = p.getUniqueId();
                    String name = p.getName();
                    return Component.text(name != null ? name : uuid.toString(), Style.style(
                            HoverEvent.showEntity(EntityType.PLAYER, uuid, name != null ? Component.text(name) : null)
                    ));
                })
                .map(c -> c.color(NamedTextColor.AQUA))
                .toList();
        return Component.join(JoinConfiguration.commas(true), components);
    }
}
