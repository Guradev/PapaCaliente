package net.gura.papaCaliente;

import lombok.Getter;
import net.gura.papaCaliente.commands.PapaCalienteCommand;
import net.gura.papaCaliente.game.GameManager;
import net.gura.papaCaliente.game.GamePlayer;
import net.gura.papaCaliente.listeners.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class PapaCaliente extends JavaPlugin {

    @Getter
    public static PapaCaliente plugin;
    @Getter
    private GameManager gameManager;

    String version = Bukkit.getVersion();

    @Override
    public void onEnable() {
        plugin = this;
        gameManager = new GameManager();

        registerCommands();

        // Registramos los eventos del plugin
        getServer().getPluginManager().registerEvents(new AdminGUIListener(), this);
        getServer().getPluginManager().registerEvents(new ModGUIListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerManagerGUIListener(), this);
        getServer().getPluginManager().registerEvents(new UserGUIListener(), this);
        getServer().getPluginManager().registerEvents(new GamePlayer(), this);

        getServer().getConsoleSender().sendMessage("Evento Papa Caliente Habilitado " + "Versión " + version);
        getServer().getConsoleSender().sendMessage("Made by Gura1");
    }

    @Override
    public void onDisable() {

        if (gameManager != null && gameManager.isRunning()) {
            gameManager.stopGame();
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setGlowing(false);
        }
        getServer().getConsoleSender().sendMessage("Evento Papa Caliente Deshabilitado " + "Versión " + version);
        getServer().getConsoleSender().sendMessage("Made by Gura1");
    }

    public void registerCommands() {
        //Crear un handler para que maneje el comando /papacaliente
        PapaCalienteCommand cmdHandler = new PapaCalienteCommand();
        getCommand("papacaliente").setExecutor(cmdHandler);
        getCommand("papacaliente").setTabCompleter(cmdHandler);
    }
}
