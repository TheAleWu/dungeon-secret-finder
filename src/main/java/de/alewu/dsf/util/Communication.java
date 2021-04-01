package de.alewu.dsf.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.ChatComponentText;

public class Communication {

    public static void chatMessage(String text) {
        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        if (thePlayer != null) {
            //Replacing § with \247 as § produces an encoding error in game
            //noinspection ConstantConditions
            thePlayer.addChatMessage(new ChatComponentText(text.replace("§", "\247")));
        }
    }

    public static void clickableChatMessage(String text) {
        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        if (thePlayer != null) {
            //Replacing § with \247 as § produces an encoding error in game
            //noinspection ConstantConditions
            ChatComponentText component = new ChatComponentText(text.replace("§", "\247"));
            thePlayer.addChatMessage(component);
        }
    }

}
