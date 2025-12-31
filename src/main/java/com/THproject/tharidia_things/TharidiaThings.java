package com.THproject.tharidia_things;

import com.THproject.tharidia_things.command.*;
import com.THproject.tharidia_things.diet.DietAttachments;
import com.THproject.tharidia_things.diet.DietDataLoader;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;

@Mod(TharidiaThings.MODID)
public class TharidiaThings {
    public static final String MODID = "tharidiathings";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TharidiaThings(IEventBus modEventBus, ModContainer modContainer) {
        // Register attachments
        DietAttachments.ATTACHMENT_TYPES.register(modEventBus);
        // Data loader will be registered via AddReloadListenerEvent
        
        LOGGER.info("Tharidia Things - Diet System Loaded");
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
