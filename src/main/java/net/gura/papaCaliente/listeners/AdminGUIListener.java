package net.gura.papaCaliente.listeners;

import net.gura.papaCaliente.PapaCaliente;
import net.gura.papaCaliente.game.GameManager;
import net.gura.papaCaliente.gui.AdminGUI;
import net.gura.papaCaliente.gui.PlayerManagerGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import static net.gura.papaCaliente.PapaCaliente.plugin;

public class AdminGUIListener implements Listener {

    @EventHandler
    public void ClickItem(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        if (!event.getView().title().equals(AdminGUI.TITLE)) return;

        event.setCancelled(true);
        Bukkit.getScheduler().runTaskLater(plugin, player::updateInventory, 1L);

        if (event.isShiftClick()) return;

        if (event.getClickedInventory() != event.getInventory()) return;

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        Material material = clickedItem.getType();
        GameManager gm = PapaCaliente.getPlugin().getGameManager();

        switch (material) {
            case LIME_WOOL -> {
                player.performCommand("papacaliente start");
            }
            case RED_WOOL -> {
                player.performCommand("papacaliente stop");
            }
            case CLOCK -> {
                if (!gm.isRunning()) {
                    player.sendMessage(Component.text("¡El evento aún no ha iniciado!").color(NamedTextColor.RED));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 10F, 1F);
                    return;
                }
                gm.getCountdown().reset(); // Resets the countdown but doesnt stop the game
                player.sendMessage(Component.text("Contador reseteado correctamente").color(NamedTextColor.GRAY));
            }
            case TNT -> {
                if (!gm.isRunning()) {
                    player.sendMessage(Component.text("¡El evento aún no ha iniciado!").color(NamedTextColor.RED));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 10F, 1F);
                    return;
                }
                if (gm.getCurrentHolder() != null) {
                    Player holder = gm.getCurrentHolder();
                    holder.sendMessage(Component.text("¡La papa caliente te explotó!").color(NamedTextColor.RED));
                    holder.getWorld().createExplosion(holder.getLocation(), 3F, false, false);
                    gm.removePlayer(holder);
                    player.sendMessage(Component.text("¡Explotado correctamente!").color(NamedTextColor.RED));
                }
            }
            case PLAYER_HEAD -> {
                PlayerManagerGUI.openGUI(player,1);
                player.sendMessage(Component.text("Abriendo menu de gestión de jugadores...").color(NamedTextColor.AQUA));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 10F, 1F);
            }
            case BARRIER -> player.closeInventory();
        }
    }
}
