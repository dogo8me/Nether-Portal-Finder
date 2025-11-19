package com.dogo8me.findportal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class FindPortalCommand {
    
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("findportal")
            .executes(FindPortalCommand::execute));
    }
    
    private static int execute(CommandContext<FabricClientCommandSource> context) {
        MinecraftClient client = context.getSource().getClient();
        
        if (client.world == null || client.player == null) {
            context.getSource().sendError(Text.literal("You must be in a world to use this command."));
            return 0;
        }
        
        FindPortalConfig config = AutoConfig.getConfigHolder(FindPortalConfig.class).getConfig();
        
        // Get player info
        double px = client.player.getX();
        double py = client.player.getY();
        double pz = client.player.getZ();
        String dimension = client.world.getRegistryKey().getValue().toString();
        
        context.getSource().sendFeedback(Text.literal("Scanning for portals...").formatted(Formatting.YELLOW));
        
        // Scan loaded area
        PortalScanner.PortalCandidate nearest = PortalScanner.findNearestInRenderDistance(client, config);
        
        if (nearest != null) {
            // Found a portal in loaded area
            String status = nearest.lit ? "lit" : "unlit";
            context.getSource().sendFeedback(
                Text.literal(String.format("Found %s portal at (%.1f, %.1f, %.1f)", status, nearest.x, nearest.y, nearest.z))
                    .formatted(Formatting.GREEN)
            );
            context.getSource().sendFeedback(
                Text.literal(String.format("Size: %dx%d, Distance: %.1f blocks", nearest.width, nearest.height, nearest.distance))
                    .formatted(Formatting.GRAY)
            );
            
            // Save to registry
            PortalData portalData = new PortalData(
                dimension,
                nearest.x,
                nearest.y,
                nearest.z,
                nearest.width,
                nearest.height,
                nearest.lit,
                System.currentTimeMillis()
            );
            PortalRegistry.addOrUpdate(portalData);
            
            return 1;
        }
        
        // No portal in loaded area, search registry
        PortalData saved = PortalRegistry.findNearest(dimension, px, py, pz, config.pruneDays);
        
        if (saved != null) {
            double dist = saved.distanceTo(px, py, pz);
            String status = saved.lit ? "lit" : "unlit";
            context.getSource().sendFeedback(
                Text.literal(String.format("No portals in loaded area. Nearest saved %s portal:", status))
                    .formatted(Formatting.YELLOW)
            );
            context.getSource().sendFeedback(
                Text.literal(String.format("Location: (%.1f, %.1f, %.1f)", saved.x, saved.y, saved.z))
                    .formatted(Formatting.GREEN)
            );
            context.getSource().sendFeedback(
                Text.literal(String.format("Size: %dx%d, Distance: %.1f blocks", saved.width, saved.height, dist))
                    .formatted(Formatting.GRAY)
            );
            
            long daysAgo = (System.currentTimeMillis() - saved.lastSeen) / (24L * 60L * 60L * 1000L);
            context.getSource().sendFeedback(
                Text.literal(String.format("Last seen: %d days ago", daysAgo))
                    .formatted(Formatting.GRAY)
            );
            
            return 1;
        }
        
        // No portals found at all
        context.getSource().sendFeedback(
            Text.literal("No portals found in loaded area or registry.")
                .formatted(Formatting.RED)
        );
        
        return 0;
    }
}
