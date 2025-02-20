package com.kinggunch.keepxpmod;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.math.BlockPos;

import java.io.File;

@Mod(modid = KeepXPMod.MODID, name = KeepXPMod.NAME, version = KeepXPMod.VERSION)
public class KeepXPMod {
    public static final String MODID = "keepxpmod";
    public static final String NAME = "KeepXPMod";
    public static final String VERSION = "1.2";

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
    public static class EventHandlers {
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
        public static void onPlayerDeathXP(PlayerEvent.Clone event) {
            if (enableXPKeep && event.isWasDeath()) { // Ensure this runs only on death, not dimension change
                EntityPlayer original = event.getOriginal();
                EntityPlayer newPlayer = event.getEntityPlayer();

                int xp = original.experienceTotal; // Get total XP before death
                
                // DEBUG LOGGING: Check XP before saving
                System.out.println("[KeepXPMod] Saving XP: " + xp);

                // Save XP to player's persistent NBT data
                NBTTagCompound playerData = newPlayer.getEntityData();
                playerData.setInteger(XP_TAG, xp);
            }
        }

        // Restore XP when the player logs in after dying (only if enabled)
        @SubscribeEvent
        public static void onPlayerRespawnXP(PlayerEvent.Clone event) {
            if (enableXPKeep && event.isWasDeath()) { // Ensure we are restoring only after death
                EntityPlayer newPlayer = event.getEntityPlayer();
                NBTTagCompound playerData = newPlayer.getEntityData();

                if (playerData.hasKey(XP_TAG)) {
                    int savedXP = playerData.getInteger(XP_TAG);

                    // DEBUG LOGGING: Check XP before restoring
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
