package net.gura.papaCaliente.commands;

import net.gura.papaCaliente.PapaCaliente;
import net.gura.papaCaliente.game.GameManager;
import net.gura.papaCaliente.gui.AdminGUI;
import net.gura.papaCaliente.gui.ModGUI;
import net.gura.papaCaliente.gui.UserGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class PapaCalienteCommand implements CommandExecutor, TabCompleter {

    GameManager gm = PapaCaliente.getPlugin().getGameManager();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, String @NotNull [] args) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(Component.text("No puedes ejecutar este comando desde la consola.").color(NamedTextColor.GOLD));
            return true;
        }

        if (!player.hasPermission("papacaliente.admin")) {
            player.sendMessage(Component.text("No tienes permisos.").color(NamedTextColor.RED));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 10F, 1F);
            return true;
        }

        if (args.length == 0) {
            AdminGUI.openGUI(player);
            return true;
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("gui")) {
            //if (args.length < 2) {
            //    return true;
            //}
            switch (args[1].toLowerCase()) {
                case "usuario":
                    UserGUI.openGUI(player);
                    break;
                case "mod":
                    ModGUI.openGUI(player);
                    break;
                case "admin":
                    AdminGUI.openGUI(player);
                    break;
            }
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("start")) {
            if (gm.isRunning()) {
                commandSender.sendMessage(Component.text("El evento ya ha sido iniciado").color(NamedTextColor.RED));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 10F, 1F);
                return true;
            }
            if (gm.getPlayers().size() < 2 && !gm.getIsTesting()) {
                commandSender.sendMessage(Component.text("No hay suficientes jugadores para iniciar el evento, requeridos 2").color(NamedTextColor.RED));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 10F, 1F);
                return true;
            }
            gm.startGame();
            commandSender.sendMessage(Component.text("Has iniciado el evento de papa caliente").color(NamedTextColor.GREEN));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 10F, 1F);
            return true;
        }

        if (args[0].equalsIgnoreCase("add")) {
            if (args.length < 2) {
                commandSender.sendMessage(Component.text( "Debes especificar un nombre de usuario").color(NamedTextColor.RED));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 10F, 1F);
                return true;
            }

            Player jugador = Bukkit.getPlayerExact(args[1]);
            if (jugador == null) {
                commandSender.sendMessage(Component.text( "No se ha encontrado al jugador").color(NamedTextColor.RED));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 10F, 1F);
                return true;
            }
            gm.addPlayer(jugador);
            commandSender.sendMessage(Component.text("Has agregado a " + jugador.getName() + " al evento").color(NamedTextColor.GREEN));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 10F, 1F);
            return true;
        }
        if (args[0].equalsIgnoreCase("remove")) {
            if (args.length < 2) {
                commandSender.sendMessage(Component.text( "Debes especificar un nombre de usuario").color(NamedTextColor.RED));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 10F, 1F);
                return true;
            }

            Player jugador = Bukkit.getPlayerExact(args[1]);
            if (jugador == null) {
                commandSender.sendMessage(Component.text("No se ha encontrado al jugador").color(NamedTextColor.RED));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 10F, 1F);
                return true;
            }
            gm.removePlayer(jugador);
            commandSender.sendMessage(Component.text("Has eliminado a " + jugador.getName() + " del evento").color(NamedTextColor.GREEN));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 10F, 1F);
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("stop")) {
            if (!gm.isRunning()) {
                commandSender.sendMessage(Component.text("El evento no está activo, inicialo para poder pararlo").color(NamedTextColor.RED));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 10F, 1F);
                return true;
            }
            gm.stopGame();
            commandSender.sendMessage(Component.text("Has parado el evento de papa caliente").color(NamedTextColor.GREEN));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 10F, 1F);
            return true;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String @NotNull [] args) {
        if (!sender.hasPermission("papacaliente.admin")) return List.of();

        if (args.length == 1) {
            List<String> completions = new ArrayList<>(List.of("start", "stop", "add", "remove", "gui"));
            completions.removeIf(s -> !s.toLowerCase().startsWith(args[0].toLowerCase()));
            return completions;
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("gui")) {
                List<String> completions = new ArrayList<>(List.of("usuario", "mod", "admin"));
                completions.removeIf(s -> !s.toLowerCase().startsWith(args[1].toLowerCase()));
                return completions;
            }
            if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        return List.of();
    }
}
