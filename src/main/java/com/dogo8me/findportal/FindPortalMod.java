package com.dogo8me.findportal;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

public class FindPortalMod implements ClientModInitializer {
    
    @Override
    public void onInitializeClient() {
        // Register config with AutoConfig
        AutoConfig.register(FindPortalConfig.class, Toml4jConfigSerializer::new);
        
        // Initialize portal registry
        PortalRegistry.init();
        
        // Register command
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            FindPortalCommand.register(dispatcher);
        });
        
        System.out.println("FindPortal mod initialized!");
    }
}
