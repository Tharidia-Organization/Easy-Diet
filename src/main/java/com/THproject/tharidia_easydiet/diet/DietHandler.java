package com.THproject.tharidia_easydiet.diet;

import com.THproject.tharidia_easydiet.TharidiaEasyDiet;
import com.THproject.tharidia_easydiet.network.DietProfileSyncPacket;
import com.THproject.tharidia_easydiet.network.DietSyncPacket;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.bus.api.SubscribeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Map;

/**
 * Handles diet gain from food consumption and periodic decay.
 */
@EventBusSubscriber(modid = TharidiaEasyDiet.MODID)
public class DietHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DietHandler.class);
    private static final float START_PERCENT = 0.8f;

    @SubscribeEvent
    public static void onFoodConsumed(LivingEntityUseItemEvent.Finish event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Player player) || entity.level().isClientSide()) {
            return;
        }

        ItemStack stack = event.getItem();
        LOGGER.info("[DIET] Item use finished: {}", stack.getItem());

        if (stack.isEmpty()) {
            return;
        }

        DietProfile profile = null;

        // Check if this is a water bottle (potion with water)
        LOGGER.info("[DIET] Is potion item? {}", stack.is(Items.POTION));
        if (stack.is(Items.POTION)) {
            LOGGER.info("[DIET] Detected potion consumption");
            PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
            LOGGER.info("[DIET] PotionContents: {}", contents);
            if (contents != null) {
                // Check if this is a water potion (no effects, matches water holder)
                boolean isWater = contents.potion()
                        .map(holder -> holder.value() == Potions.WATER.value())
                        .orElse(contents.customEffects().isEmpty() && contents.customColor().isEmpty());

                LOGGER.info("[DIET] Is water bottle: {}", isWater);

                if (isWater) {
                    // Water bottle provides water nutrition
                    DietSystemSettings settings = DietRegistry.getSettings();
                    float waterValue = settings.waterAlwaysEatBonus() + settings.drinkWaterBonus();
                    profile = DietProfile.of(0, 0, 0, 0, 0, waterValue);
                    LOGGER.info("[DIET] Water bottle nutrition applied: {}", waterValue);
                }
            }
        }

        // If not a water bottle, check for regular food
        if (profile == null) {
            if (stack.getItem().getFoodProperties(stack, player) == null) {
                return;
            }
            profile = DietRegistry.getProfile(stack);
        }

        if (profile == null || profile.isEmpty()) {
            return;
        }

        ServerPlayer serverPlayer = (ServerPlayer) player;
        DietData data = serverPlayer.getData(DietAttachments.DIET_DATA);
        initializeIfNeeded(serverPlayer, data);
        data.add(profile, DietRegistry.getMaxValues());
        data.setLastDecayTimeMs(System.currentTimeMillis());
        syncIfNeeded(serverPlayer, data, true);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        DietData data = player.getData(DietAttachments.DIET_DATA);
        initializeIfNeeded(serverPlayer, data);
        DietEffectApplier.apply(serverPlayer, data);
        
        DietSystemSettings settings = DietRegistry.getSettings();
        long decayIntervalMs = settings.decayIntervalMillis();
        long now = System.currentTimeMillis();
        long lastDecay = data.getLastDecayTimeMs();
        long timeSinceDecay = now - lastDecay;
        
        if (timeSinceDecay < decayIntervalMs) {
            return;
        }
        
        float elapsedSeconds = timeSinceDecay / 1000.0f;
        float intervalSeconds = Math.max(1.0f, decayIntervalMs / 1000.0f);
        float intervalUnits = elapsedSeconds / intervalSeconds;
        boolean changed = data.applyDecay(DietRegistry.getDecayRates(), intervalUnits);
        data.setLastDecayTimeMs(now);

        if (changed) {
            syncIfNeeded(serverPlayer, data, false);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }
        
        // Set server reference for recipe analysis
        DietRegistry.setServer(serverPlayer.getServer());
        
        DietData data = serverPlayer.getData(DietAttachments.DIET_DATA);
        initializeIfNeeded(serverPlayer, data);

        // Reset decay timer to current time to prevent decay during offline time
        data.setLastDecayTimeMs(System.currentTimeMillis());

        DietEffectApplier.apply(serverPlayer, data);
        syncIfNeeded(serverPlayer, data, true);
        
        // Sync diet profiles from server to client
        syncDietProfilesToClient(serverPlayer);
    }
    
    private static void syncDietProfilesToClient(ServerPlayer player) {
        // Get all cached profiles from server
        DietProfileCache serverCache = DietRegistry.getPersistentCache();
        if (serverCache != null) {
            Map<ResourceLocation, DietProfile> profiles = serverCache.getAllProfiles();
            if (!profiles.isEmpty()) {
                PacketDistributor.sendToPlayer(player, new DietProfileSyncPacket(profiles));
            }
        }
    }

    private static void syncIfNeeded(ServerPlayer player, DietData data, boolean force) {
        boolean dirty = force || data.consumeDirty();
        if (!dirty) {
            return;
        }

        float[] values = data.copyValues();
        PacketDistributor.sendToPlayer(player, new DietSyncPacket(values));
    }

    private static void initializeIfNeeded(ServerPlayer player, DietData data) {
        if (data.isInitialized()) {
            return;
        }
        data.ensureInitialized(DietRegistry.getMaxValues(), START_PERCENT);
        data.setLastDecayTimeMs(System.currentTimeMillis());
        syncIfNeeded(player, data, true);
    }
}
