package com.THproject.tharidia_easydiet.network;

import com.THproject.tharidia_easydiet.TharidiaEasyDiet;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = TharidiaEasyDiet.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ClientPacketHandler {
    
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        
        registrar.playToClient(
            DietSyncPacket.TYPE,
            DietSyncPacket.STREAM_CODEC,
            (packet, context) -> context.enqueueWork(() -> 
                DietSyncPacket.handle(packet, context.player())
            )
        );
        
        registrar.playToClient(
            DietProfileSyncPacket.TYPE,
            DietProfileSyncPacket.STREAM_CODEC,
            (packet, context) -> context.enqueueWork(() -> 
                DietProfileSyncPacket.handle(packet, context.player())
            )
        );
        
        TharidiaEasyDiet.LOGGER.info("Diet network packets registered");
    }
}
