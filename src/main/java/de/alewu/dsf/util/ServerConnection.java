package de.alewu.dsf.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.StringUtils;

public class ServerConnection {

    private ServerConnection() {
        // Util class
    }

    private static boolean isOnHypixel() {
        ServerData serverData = Minecraft.getMinecraft().getCurrentServerData();
        return serverData != null && serverData.serverIP.contains("hypixel.net");
    }

    private static boolean isOnSkyblock() {
        if (Minecraft.getMinecraft().thePlayer == null) {
            return false;
        }
        Scoreboard scoreboard = Minecraft.getMinecraft().thePlayer.getWorldScoreboard();
        return scoreboard != null && scoreboard.getObjectiveInDisplaySlot(1) != null
            && StringUtils.stripControlCodes(scoreboard.getObjectiveInDisplaySlot(1).getDisplayName()).contains("SKYBLOCK");
    }

    private static boolean isInDungeons() {
        if (Minecraft.getMinecraft().thePlayer == null) {
            return false;
        }
        Scoreboard scoreboard = Minecraft.getMinecraft().thePlayer.getWorldScoreboard();
        if (scoreboard == null) {
            return false;
        }
        return scoreboard.getScores().stream().anyMatch(x -> {
            if (scoreboard.getPlayersTeam(x.getPlayerName()) == null || scoreboard.getPlayersTeam(x.getPlayerName()).formatString("") == null) {
                return false;
            }
            return StringUtils.stripControlCodes(scoreboard.getPlayersTeam(x.getPlayerName()).formatString("")).contains("The Catacombs");
        });
    }

    public static boolean isInvalid() {
        return !isOnHypixel() || !isOnSkyblock() || !isInDungeons();
    }

}
