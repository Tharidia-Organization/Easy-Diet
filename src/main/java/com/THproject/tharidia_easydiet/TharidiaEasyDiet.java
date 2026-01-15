package com.THproject.tharidia_easydiet;

import com.THproject.tharidia_easydiet.command.*;
import com.THproject.tharidia_easydiet.diet.DietAttachments;
import com.THproject.tharidia_easydiet.diet.DietDataLoader;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;

@Mod(TharidiaEasyDiet.MODID)
@EventBusSubscriber(modid = TharidiaEasyDiet.MODID)
public class TharidiaEasyDiet {
    public static final String MODID = "tharidia_easydiet";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TharidiaEasyDiet(IEventBus modEventBus, ModContainer modContainer) {
        // Register attachments
        DietAttachments.ATTACHMENT_TYPES.register(modEventBus);
        // Data loader will be registered via AddReloadListenerEvent
        
        LOGGER.info("Tharidia Easy Diet System Loaded");
    }

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new DietDataLoader());
        LOGGER.info("Diet data loader registered");
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        // Register commands here
        LOGGER.info("Commands registered");
    }

}
