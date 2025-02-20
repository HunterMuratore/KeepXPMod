package com.kinggunch.keepxpmod;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;

@Mod(modid = KeepXPMod.MODID, name = KeepXPMod.NAME, version = KeepXPMod.VERSION)
public class KeepXPMod {
    public static final String MODID = "keepxpmod";
    public static final String NAME = "KeepXPMod - Respawn Location";
    public static final String VERSION = "1.3";

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        System.out.println(NAME + " is loaded!");
    }

    @Mod.EventBusSubscriber
    public static class RespawnHandler {
        private static final String DEATH_DATA = "KeepXPModData";

        // Save player's exact death location on death
        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onPlayerDeath(LivingDeathEvent event) {
            if (event.getEntity() instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) event.getEntity();
                BlockPos deathPos = player.getPosition();

                // DEBUG: Log death position
                System.out.println("[KeepXPMod] Saving Death Location: " + deathPos);

                // Save death location in player's persistent NBT data
                NBTTagCompound data = player.getEntityData();
                NBTTagCompound deathData = new NBTTagCompound();
                deathData.setInteger("SavedX", deathPos.getX());
                deathData.setInteger("SavedY", deathPos.getY());
                deathData.setInteger("SavedZ", deathPos.getZ());
                data.setTag(DEATH_DATA, deathData);
            }
        }

        // Restore player to death location and set Creative mode on respawn
        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onPlayerRespawn(PlayerEvent.Clone event) {
            if (event.isWasDeath()) { // Ensure this runs only on death, not dimension change
                EntityPlayer newPlayer = event.getEntityPlayer();
                NBTTagCompound data = newPlayer.getEntityData();

                if (data.hasKey(DEATH_DATA)) {
                    NBTTagCompound deathData = data.getCompoundTag(DEATH_DATA);
                    int savedX = deathData.getInteger("SavedX");
                    int savedY = deathData.getInteger("SavedY");
                    int savedZ = deathData.getInteger("SavedZ");

                    // DEBUG: Log teleportation location
                    System.out.println("[KeepXPMod] Teleporting to: " + savedX + ", " + savedY + ", " + savedZ);

                    // Teleport player to saved death location
                    newPlayer.setPositionAndUpdate(savedX + 0.5, savedY, savedZ + 0.5);

                    // Set player to creative mode to prevent instant death
                    newPlayer.setGameType(GameType.CREATIVE);

                    // Clear stored location after respawn
                    data.removeTag(DEATH_DATA);
                } else {
                    System.out.println("[KeepXPMod] No saved location found, respawning normally.");
                }
            }
        }
    }
}
