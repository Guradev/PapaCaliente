package net.gura.papaCaliente.game;

import lombok.Getter;
import lombok.Setter;
import net.gura.papaCaliente.logics.Countdown;
import net.gura.papaCaliente.ui.BossBarHandler;
import net.gura.papaCaliente.utils.CustomItems;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.time.Duration;
import java.util.*;

import static net.gura.papaCaliente.PapaCaliente.plugin;

public class GameManager {
    @Getter
    private Countdown countdown;
    @Getter
    private GameState gameState = GameState.ESPERANDO;
    private final Set<Player> players = new HashSet<>();
    @Setter
    @Getter
    private Player currentHolder = null;
    BossBarHandler bossbar = new BossBarHandler("Papa Caliente...", 1f, BossBar.Color.YELLOW, BossBar.Overlay.PROGRESS);

    private final boolean isTesting = false; // For testing purposes only (disable in production)

    public boolean isRunning() {
        return gameState == GameState.CORRIENDO;
    }

    public boolean getIsTesting() {
        return isTesting;
    }

    public void addPlayer(Player player) {
        if (players.contains(player)) return;
        players.add(player);
        player.sendMessage(Component.text("¡Fuiste agregado al evento de papa caliente!").color(NamedTextColor.GOLD));
    }

    public void removePlayer(Player player) {

        players.remove(player);

        if (player.equals(currentHolder)) {
            removePotato(player);
            currentHolder = null;
        }
    }

    public boolean isInGame(Player player) {
        return players.contains(player);
    }

    public void startGame() {
        if (players.size() < 2 && !isTesting) {
            return;
        }

        List<Player> listaPlayers = new ArrayList<>(players);
        if (listaPlayers.isEmpty()) {
            return;
        }

        gameState = GameState.CORRIENDO;

        //Elige a una persona random de la lista para darle la papa caliente
        Collections.shuffle(listaPlayers);
        currentHolder = listaPlayers.getFirst();

        givePotato(currentHolder);
        currentHolder.setGlowing(true);

        countdown = new Countdown(plugin, 10,
                secondsLeft -> {
                    bossbar.ShowToAll(players);

                    float progress = Math.max(0f, secondsLeft / 10f);
                    bossbar.updateProgress(progress);
                    bossbar.updateTitle("⏳ ¡" + secondsLeft + "s para explotar!");
                    /* Send hurt player packet to simulate screen flashing for aesthetics
                     Screen.flashRed(currentHolder, plugin, 5,10);
                     REQUIRES FIXING
                    */
                    currentHolder.sendMessage(
                            Component.text("¡La papa explotará en ", NamedTextColor.GOLD)
                                    .append(Component.text(secondsLeft + "s", NamedTextColor.WHITE))
                                    .append(Component.text("!", NamedTextColor.GOLD))
                    );
                },
                () -> {
                    currentHolder.damage(100);
                    removePotato(currentHolder);
                    currentHolder.sendMessage(Component.text("¡La papa te explotó!").color(NamedTextColor.RED));
                    // Lanzamos un packet a todos los jugadores conectados para que vean las particulas
                    removePlayer(currentHolder);

                    bossbar.HideToAll();

                    if (players.size() < 2) {
                        stopGame();
                    } else {
                        startGame();
                    }
                }
        );
        countdown.start();
    }

    public void stopGame() {
        gameState = GameState.TERMINADO;
        Bukkit.getScheduler().runTaskLater(plugin, bossbar::HideToAll, 3L);

        if (countdown != null) {
            countdown.cancel();
            countdown = null;
        }
        for (Player player : new ArrayList<>(Bukkit.getOnlinePlayers())) {
            player.setGlowing(false);
            ItemStack[] contents = player.getInventory().getContents();
            for (int i = 0; i < contents.length; i++) {
                ItemStack item = contents[i];
                if (CustomItems.isPapaCaliente(item)) {
                    contents[i] = null;
                }
            }
            player.getInventory().setContents(contents);
        }

        Player winner = players.stream().findFirst().orElse(null);

        if (winner == null) return;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 3F, 1F);
                        Title title1 = Title.title(
                                Component.text("🔥 ¡Épico! 🔥")
                                        .color(NamedTextColor.GOLD)
                                        .decorate(TextDecoration.BOLD),
                                Component.text("La papa ha explotado...").color(NamedTextColor.RED),
                                Title.Times.times(Duration.ofMillis(100), Duration.ofSeconds(1), Duration.ofMillis(500))
                        );
                        player.showTitle(title1);
                    }
    },40L);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 3F, 1F);
                player.showTitle(Title.title(
                        Component.text("🏆 GANADOR: ")
                                .color(NamedTextColor.YELLOW)
                                .decorate(TextDecoration.BOLD)
                                .append(Component.text(winner.getName(), NamedTextColor.GOLD)),
                        Component.text("¡Gracias por jugar!").color(NamedTextColor.GRAY),
                        Title.Times.times(Duration.ofMillis(300), Duration.ofSeconds(2), Duration.ofMillis(300))
                ));
            }
        }, 40L);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                Title title3 = Title.title(
                        Component.text("¡Gracias por jugar!").color(NamedTextColor.AQUA),
                        Component.empty(),
                        Title.Times.times(Duration.ofSeconds(2), Duration.ofSeconds(5), Duration.ofSeconds(2))
                );
                player.showTitle(title3);
            }
        }, 100L);


        players.clear();
        currentHolder = null;
        gameState = GameState.ESPERANDO;
    }

    public Set<Player> getPlayers() {
        return Collections.unmodifiableSet(players);
    }

    public void passPotato(Player de, Player a) {
        if (de == null || a == null) return;
        if (!players.contains(de) || !players.contains(a)) return;
        if (de.equals(a)) return;
        if (!de.equals(currentHolder)) return;

        de.setGlowing(false);
        a.setGlowing(true);

        removePotato(de);
        givePotato(a);

        currentHolder = a;
    }

    private void givePotato(Player player) {
        ItemStack papa = CustomItems.PapaCaliente(player);
        player.getInventory().setItem(0, papa);
        Bukkit.getScheduler().runTaskLater(plugin, player::updateInventory, 1L);
    }

    private void removePotato(Player player) {
        ItemStack item = player.getInventory().getItem(0);
        if (CustomItems.isPapaCaliente(item)) {
            player.getInventory().setItem(0, null);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 10F, 1F);
        }
    }
}
