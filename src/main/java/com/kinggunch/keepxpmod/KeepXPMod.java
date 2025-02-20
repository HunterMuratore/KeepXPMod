package com.kinggunch.keepxpmod;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import java.io.File;

@Mod(modid = KeepXPMod.MODID, name = KeepXPMod.NAME, version = KeepXPMod.VERSION)
public class KeepXPMod {
    public static final String MODID = "keepxpmod";
    public static final String NAME = "Keep XP on Death Mod";
    public static final String VERSION = "1.1";

    // Config option
    public static boolean enableXPKeep = true;

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
        public static void onPlayerRespawn(PlayerEvent.Clone event) {
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
    }
}
