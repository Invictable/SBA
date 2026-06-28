package io.github.pronze.sba;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.screamingsandals.bedwars.api.game.Game;

import java.io.File;

public class InvictoolsDataGrabber
{
    Plugin plugin = Bukkit.getPluginManager().getPlugin("Invictools");
    File Folder = new File(plugin.getDataFolder(), "Maps");

    public String getGameType(Game game)
    {
        File pFile = new File(Folder, game.getName() + ".yml");
        final FileConfiguration mapData = YamlConfiguration.loadConfiguration(pFile);
        String type = mapData.getString("GameType");
        if (type == null)
            return "normal";
        else
            return type;
    }

    /*
    additions:
    trap length, syntax modification, playerlistener added colored item names,
    playerlistener getgametype, bedfight reduces respawn
    shop util removed wood and gold tools, armor enchanter is now unsafe enchantments to save elytra from bugging
    partyinvitecommand made limits higher
     */
}
