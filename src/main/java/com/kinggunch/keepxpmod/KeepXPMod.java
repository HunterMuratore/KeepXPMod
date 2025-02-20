package com.kinggunch.keepxpmod;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.io.File;

@Mod(modid = KeepXPMod.MODID, name = KeepXPMod.NAME, version = KeepXPMod.VERSION)
public class KeepXPMod {
    public static final String MODID = "keepxpmod";
    public static final String NAME = "KeepXPMod";
    public static final String VERSION = "1.3";

    // Config options
    public static boolean enableXPKeep = true;
    public static boolean enableTeleportMessage = true;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        loadConfig(new File(event.getModConfigurationDirectory(), "keepxpmod.cfg"));
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        System.out.println(NAME + " is loaded!");
    }

    private void loadConfig(File file) {
        Configuration config = new Configuration(file);
        enableXPKeep = config.getBoolean("enableXPKeep", "General", true, "Set to false to disable XP saving.");
        enableTeleportMessage = config.getBoolean("enableTeleportMessage", "General", true, "Set to false to disable the death location teleport message.");

        if (config.hasChanged()) {
            config.save();
        }
    }

    @Mod.EventBusSubscriber
    public static class XPHandler {
        private static final String XP_TAG = "SavedXP";

        // Prevent XP from dropping on death (only if enabled)
        @SubscribeEvent
        public static void preventXPDrop(LivingExperienceDropEvent event) {
            if (enableXPKeep && event.getEntity() instanceof EntityPlayer) {
                event.setCanceled(true); // Stops XP orbs from dropping
            }
        }

        // Save XP when the player dies (only if enabled)
        @SubscribeEvent
        public static void onPlayerDeath(PlayerEvent.Clone event) {
            if (enableXPKeep && event.isWasDeath()) {
                EntityPlayer original = event.getOriginal();
                EntityPlayer newPlayer = event.getEntityPlayer();

                int xp = original.experienceTotal; // Get total XP before death

                // DEBUG LOGGING
                System.out.println("[KeepXPMod] Saving XP: " + xp);

                // Save XP to player's persistent NBT data
                NBTTagCompound playerData = newPlayer.getEntityData();
                playerData.setInteger(XP_TAG, xp);
            }
        }

        // Restore XP when the player logs in after dying (only if enabled)
        @SubscribeEvent
        public static void onPlayerRespawn(PlayerEvent.Clone event) {
            if (enableXPKeep && event.isWasDeath()) {
                EntityPlayer newPlayer = event.getEntityPlayer();
                NBTTagCompound playerData = newPlayer.getEntityData();

                if (playerData.hasKey(XP_TAG)) {
                    int savedXP = playerData.getInteger(XP_TAG);

                    // DEBUG LOGGING
                    System.out.println("[KeepXPMod] Restoring XP: " + savedXP);

                    // Restore XP properly
                    newPlayer.addExperience(savedXP);

                    // Clear stored XP after restoring
                    playerData.removeTag(XP_TAG);
                } else {
                    System.out.println("[KeepXPMod] No saved XP found!");
                }
            }
        }

        // Save player's death location and send a teleport message (only if enabled)
        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onPlayerDeathLocation(LivingDeathEvent event) {
            if (enableTeleportMessage && event.getEntity() instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) event.getEntity();
                BlockPos deathPos = player.getPosition();
                World world = player.getEntityWorld();

                // DEBUG: Log death position
                System.out.println("[KeepXPMod] Saving Death Location: " + deathPos);

                // Create a clickable teleport message
                String command = "/tp " + player.getName() + " " + deathPos.getX() + " " + deathPos.getY() + " " + deathPos.getZ();
                TextComponentString message = new TextComponentString("Click here to return to your death location");
                message.setStyle(new Style()
                        .setUnderlined(true)  // Underlines text
                        .setColor(net.minecraft.util.text.TextFormatting.GOLD)  // Sets color to gold
                        .setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command)));

                // Only send message on the client side
                if (!world.isRemote) {
                    player.sendMessage(message);
                }
            }
        }
    }
}
