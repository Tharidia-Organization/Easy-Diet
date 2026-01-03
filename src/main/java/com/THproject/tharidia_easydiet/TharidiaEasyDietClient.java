package com.THproject.tharidia_easydiet;

import com.THproject.tharidia_easydiet.diet.ClientDietProfileCache;
import com.THproject.tharidia_easydiet.diet.DietRegistry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = TharidiaEasyDiet.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = TharidiaEasyDiet.MODID, value = Dist.CLIENT)
public class TharidiaEasyDietClient {
    private static ClientDietProfileCache clientDietCache = null;
    
    public TharidiaEasyDietClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Register block entity renderers
        event.enqueueWork(() -> {
            // Initialize client diet profile cache
            initializeClientDietCache();
        });
    }
    
    private static void initializeClientDietCache() {
        try {
            clientDietCache = new ClientDietProfileCache();
            clientDietCache.load();
            
            // Start background calculation if needed
            if (clientDietCache.needsRecalculation()) {
                TharidiaEasyDiet.LOGGER.info("[DIET CLIENT] Starting background calculation of diet profiles...");
                clientDietCache.calculateAsync(DietRegistry.getSettings());
            } else {
                TharidiaEasyDiet.LOGGER.info("[DIET CLIENT] Using cached diet profiles");
            }
        } catch (Exception e) {
            TharidiaEasyDiet.LOGGER.error("[DIET CLIENT] Failed to initialize client diet cache", e);
            clientDietCache = null;
        }
    }
    
    public static ClientDietProfileCache getClientDietCache() {
        return clientDietCache;
    }
}
