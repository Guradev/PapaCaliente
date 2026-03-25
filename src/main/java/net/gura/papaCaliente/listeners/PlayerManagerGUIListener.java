package net.gura.papaCaliente.listeners;

import net.gura.papaCaliente.PapaCaliente;
import net.gura.papaCaliente.game.GameManager;
import net.gura.papaCaliente.gui.AdminGUI;
import net.gura.papaCaliente.gui.PlayerManagerGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayerManagerGUIListener implements Listener {

    GameManager gm = PapaCaliente.getPlugin().getGameManager();

    private final Pattern pagePattern = Pattern.compile("\\[(\\d+)/(\\d+)]");
    private final Set<Player> players = gm.getPlayers();

    @EventHandler
    public void ClickItem(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player admin)) return;

        String title = event.getView().title().toString();

        if (!title.contains("ɢᴇꜱᴛɪóɴ ᴊᴜɢᴀᴅᴏʀᴇꜱ")) return;
        event.setCancelled(true);

        if (event.getClickedInventory() != event.getInventory()) return;

        GameManager gm = PapaCaliente.getPlugin().getGameManager();
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        Material type = clickedItem.getType();

        int currentPage = 0;
        Matcher matcher = pagePattern.matcher(title);
        if (matcher.find()) {
            currentPage = Integer.parseInt(matcher.group(1)) - 1;
        }

        switch (type) {
            case ARROW -> {
                ItemMeta meta = clickedItem.getItemMeta();
                if (meta == null) return;
                Component nameComponent = meta.displayName();
                if (nameComponent == null) return;
                String plainName = PlainTextComponentSerializer.plainText().serialize(nameComponent);
                if (plainName.contains("ʀᴇᴛʀᴏᴄᴇᴅᴇʀ")) {
                    if (currentPage > 0) {
                        PlayerManagerGUI.openGUI(admin, currentPage - 1);
                    } else {
                        AdminGUI.openGUI(admin);
                    }
                } else if (plainName.contains("ᴀᴠᴀɴᴢᴀʀ")) {
                    PlayerManagerGUI.openGUI(admin, currentPage + 1);
                }
            }

            case BARRIER -> admin.closeInventory();

            case SLIME_BALL -> {
                if (players.size() == Bukkit.getOnlinePlayers().size()) {
                    admin.sendMessage(Component.text("Todos los jugadores ya fueron añadidos al evento").color(NamedTextColor.RED));
                    admin.playSound(admin.getLocation(), Sound.ENTITY_VILLAGER_NO, 10F, 1F);
                    return;
                }
                Bukkit.getOnlinePlayers().forEach(gm::addPlayer);
                admin.sendMessage(Component.text("Todos los jugadores online fueron añadidos al evento").color(NamedTextColor.GREEN));
                admin.playSound(admin.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 10F, 1F);
                PlayerManagerGUI.openGUI(admin, currentPage);
            }

            case TNT -> {
                if (players.isEmpty()) {
                    admin.sendMessage(Component.text("No hay jugadores en el evento a remover").color(NamedTextColor.RED));
                    admin.playSound(admin.getLocation(), Sound.ENTITY_VILLAGER_NO, 10F, 1F);
                    return;
                }
                new ArrayList<>(gm.getPlayers()).forEach(gm::removePlayer);
                admin.sendMessage(Component.text("Todos los jugadores fueron eliminados del evento").color(NamedTextColor.GREEN));
                admin.playSound(admin.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 10F, 1F);
                PlayerManagerGUI.openGUI(admin, currentPage);
            }

            case PLAYER_HEAD -> {
                if (!(clickedItem.getItemMeta() instanceof SkullMeta skullMeta)) return;

                OfflinePlayer target = skullMeta.getOwningPlayer();
                if (target == null || !target.isOnline()) {
                    admin.sendMessage(Component.text("Jugador no disponible").color(NamedTextColor.RED));
                    return;
                }

                Player targetPlayer = (Player) target;

                if (event.getClick() == ClickType.LEFT) {
                    admin.teleport(targetPlayer);
                    admin.sendMessage(Component.text("Teletransportando a ").color(NamedTextColor.GRAY).append(Component.text(targetPlayer.getName(), NamedTextColor.WHITE)));
                    admin.playSound(admin.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1F, 1F);
                } else if (event.getClick() == ClickType.RIGHT) {
                    gm.removePlayer(targetPlayer);
                    admin.sendMessage(Component.text(targetPlayer.getName() + " fue eliminado del evento").color(NamedTextColor.RED));
                    targetPlayer.sendMessage(Component.text("Has sido eliminado del evento").color(NamedTextColor.RED));
                    PlayerManagerGUI.openGUI(admin, currentPage);
                }
            }
        }
    }
}