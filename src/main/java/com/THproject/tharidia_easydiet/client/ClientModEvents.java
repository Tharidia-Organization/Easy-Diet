package com.THproject.tharidia_easydiet.client;

import com.THproject.tharidia_easydiet.TharidiaEasyDiet;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Client-side mod event handlers - Diet only
 */
@EventBusSubscriber(modid = TharidiaEasyDiet.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        TharidiaEasyDiet.LOGGER.info("Diet client setup complete");
    }
}
