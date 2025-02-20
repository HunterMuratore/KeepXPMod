package com.kinggunch.keepxpmod;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.math.BlockPos;

@Mod(modid = KeepXPMod.MODID, name = KeepXPMod.NAME, version = KeepXPMod.VERSION)
public class KeepXPMod {
    public static final String MODID = "keepxpmod";
    public static final String NAME = "KeepXPMod - Death Location Link";
    public static final String VERSION = "1.4";

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        System.out.println(NAME + " is loaded!");
    }

    @Mod.EventBusSubscriber
    public static class DeathLocationHandler {
        
        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onPlayerDeath(LivingDeathEvent event) {
            if (event.getEntity() instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) event.getEntity();
                BlockPos deathPos = player.getPosition();

                // DEBUG: Log death position
                System.out.println("[KeepXPMod] Saving Death Location: " + deathPos);

                // Create a clickable teleport message
                String command = "/tp " + player.getName() + " " + deathPos.getX() + " " + deathPos.getY() + " " + deathPos.getZ();
                TextComponentString message = new TextComponentString("§6[Click here to return to your death location]");
                message.setStyle(new Style().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command)));

                // Send the clickable message to the player
                player.sendMessage(message);
            }
        }
    }
}
