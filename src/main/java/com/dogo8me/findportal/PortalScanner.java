package com.dogo8me.findportal;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PortalScanner {
    
    public static class PortalCandidate {
        public double x, y, z;
        public int width, height;
        public boolean lit;
        public double distance;
        
        public PortalCandidate(double x, double y, double z, int width, int height, boolean lit, double distance) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.width = width;
            this.height = height;
            this.lit = lit;
            this.distance = distance;
        }
    }
    
    public static PortalCandidate findNearestInRenderDistance(MinecraftClient client, FindPortalConfig config) {
        if (client.world == null || client.player == null) {
            return null;
        }
        
        ClientWorld world = client.world;
        Vec3d playerPos = client.player.getPos();
        int renderDistance = client.options.getViewDistance().getValue();
        int maxRadius = Math.min(renderDistance * 16, config.maxScanRadius);
        
        List<PortalCandidate> candidates = new ArrayList<>();
        
        // Parse valid portal sizes
        List<PortalSize> validSizes = parsePortalSizes(config.validPortalSizes);
        
        // Scan for lit portals (portal blocks)
        candidates.addAll(scanForLitPortals(world, playerPos, maxRadius, validSizes));
        
        // Scan for unlit frames if enabled
        if (config.showUnlitPortals) {
            candidates.addAll(scanForUnlitFrames(world, playerPos, maxRadius, validSizes));
        }
        
        // Find nearest
        PortalCandidate nearest = null;
        double minDist = Double.MAX_VALUE;
        for (PortalCandidate c : candidates) {
            if (c.distance < minDist) {
                minDist = c.distance;
                nearest = c;
            }
        }
        
        return nearest;
    }
    
    private static List<PortalCandidate> scanForLitPortals(ClientWorld world, Vec3d playerPos, int maxRadius, List<PortalSize> validSizes) {
        List<PortalCandidate> results = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        
        int px = (int) playerPos.x;
        int py = (int) playerPos.y;
        int pz = (int) playerPos.z;
        
        // Scan in a cube around player
        for (int x = px - maxRadius; x <= px + maxRadius; x++) {
            for (int z = pz - maxRadius; z <= pz + maxRadius; z++) {
                for (int y = Math.max(world.getBottomY(), py - 64); y <= Math.min(world.getTopY() - 1, py + 64); y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (visited.contains(pos)) continue;
                    
                    BlockState state = world.getBlockState(pos);
                    if (state.isOf(Blocks.NETHER_PORTAL)) {
                        PortalCandidate portal = analyzePortal(world, pos, playerPos, validSizes, visited);
                        if (portal != null) {
                            results.add(portal);
                        }
                    }
                }
            }
        }
        
        return results;
    }
    
    private static PortalCandidate analyzePortal(ClientWorld world, BlockPos start, Vec3d playerPos, List<PortalSize> validSizes, Set<BlockPos> visited) {
        // Flood fill to find all connected portal blocks
        Set<BlockPos> portalBlocks = new HashSet<>();
        floodFillPortal(world, start, portalBlocks, visited);
        
        if (portalBlocks.isEmpty()) return null;
        
        // Determine portal orientation and bounds
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
        
        for (BlockPos p : portalBlocks) {
            minX = Math.min(minX, p.getX());
            maxX = Math.max(maxX, p.getX());
            minY = Math.min(minY, p.getY());
            maxY = Math.max(maxY, p.getY());
            minZ = Math.min(minZ, p.getZ());
            maxZ = Math.max(maxZ, p.getZ());
        }
        
        int width = Math.max(maxX - minX + 1, maxZ - minZ + 1);
        int height = maxY - minY + 1;
        
        // Check if size is valid
        boolean validSize = false;
        for (PortalSize size : validSizes) {
            if (width == size.width && height == size.height) {
                validSize = true;
                break;
            }
        }
        
        if (!validSize) return null;
        
        double cx = (minX + maxX) / 2.0;
        double cy = (minY + maxY) / 2.0;
        double cz = (minZ + maxZ) / 2.0;
        
        double dist = playerPos.distanceTo(new Vec3d(cx, cy, cz));
        
        return new PortalCandidate(cx, cy, cz, width, height, true, dist);
    }
    
    private static void floodFillPortal(ClientWorld world, BlockPos start, Set<BlockPos> result, Set<BlockPos> visited) {
        if (!world.getBlockState(start).isOf(Blocks.NETHER_PORTAL)) return;
        if (visited.contains(start)) return;
        
        visited.add(start);
        result.add(start);
        
        // Check neighbors
        floodFillPortal(world, start.up(), result, visited);
        floodFillPortal(world, start.down(), result, visited);
        floodFillPortal(world, start.north(), result, visited);
        floodFillPortal(world, start.south(), result, visited);
        floodFillPortal(world, start.east(), result, visited);
        floodFillPortal(world, start.west(), result, visited);
    }
    
    private static List<PortalCandidate> scanForUnlitFrames(ClientWorld world, Vec3d playerPos, int maxRadius, List<PortalSize> validSizes) {
        List<PortalCandidate> results = new ArrayList<>();
        
        int px = (int) playerPos.x;
        int py = (int) playerPos.y;
        int pz = (int) playerPos.z;
        
        // Scan in a more limited range for performance
        int frameRadius = Math.min(maxRadius, 128);
        
        for (int x = px - frameRadius; x <= px + frameRadius; x += 2) {
            for (int z = pz - frameRadius; z <= pz + frameRadius; z += 2) {
                for (int y = Math.max(world.getBottomY(), py - 32); y <= Math.min(world.getTopY() - 10, py + 32); y += 2) {
                    BlockPos pos = new BlockPos(x, y, z);
                    
                    // Try to detect frames in both orientations
                    for (PortalSize size : validSizes) {
                        PortalCandidate frame = checkForFrame(world, pos, size, true, playerPos); // X-aligned
                        if (frame != null) {
                            results.add(frame);
                            continue;
                        }
                        
                        frame = checkForFrame(world, pos, size, false, playerPos); // Z-aligned
                        if (frame != null) {
                            results.add(frame);
                        }
                    }
                }
            }
        }
        
        return results;
    }
    
    private static PortalCandidate checkForFrame(ClientWorld world, BlockPos cornerPos, PortalSize size, boolean xAligned, Vec3d playerPos) {
        // Check if there's a rectangular frame at this position
        int width = size.width;
        int height = size.height;
        
        // Check corners and edges for non-air blocks
        // Check interior for all air blocks
        
        boolean hasFrame = true;
        boolean interiorEmpty = true;
        
        for (int i = 0; i < width + 2; i++) {
            for (int j = 0; j < height + 2; j++) {
                boolean isEdge = (i == 0 || i == width + 1 || j == 0 || j == height + 1);
                
                BlockPos checkPos;
                if (xAligned) {
                    checkPos = cornerPos.add(0, j, i);
                } else {
                    checkPos = cornerPos.add(i, j, 0);
                }
                
                BlockState state = world.getBlockState(checkPos);
                
                if (isEdge) {
                    // Frame should be non-air
                    if (state.isAir()) {
                        hasFrame = false;
                        break;
                    }
                } else {
                    // Interior should be air (unlit portal)
                    if (!state.isAir()) {
                        interiorEmpty = false;
                        break;
                    }
                }
            }
            if (!hasFrame || !interiorEmpty) break;
        }
        
        if (!hasFrame || !interiorEmpty) return null;
        
        // Calculate center position
        double cx, cy, cz;
        if (xAligned) {
            cx = cornerPos.getX();
            cy = cornerPos.getY() + (height + 1) / 2.0;
            cz = cornerPos.getZ() + (width + 1) / 2.0;
        } else {
            cx = cornerPos.getX() + (width + 1) / 2.0;
            cy = cornerPos.getY() + (height + 1) / 2.0;
            cz = cornerPos.getZ();
        }
        
        double dist = playerPos.distanceTo(new Vec3d(cx, cy, cz));
        
        return new PortalCandidate(cx, cy, cz, width, height, false, dist);
    }
    
    private static class PortalSize {
        int width;
        int height;
        
        PortalSize(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }
    
    private static List<PortalSize> parsePortalSizes(List<String> sizeStrings) {
        List<PortalSize> sizes = new ArrayList<>();
        for (String s : sizeStrings) {
            String[] parts = s.toLowerCase().split("x");
            if (parts.length == 2) {
                try {
                    int w = Integer.parseInt(parts[0].trim());
                    int h = Integer.parseInt(parts[1].trim());
                    sizes.add(new PortalSize(w, h));
                } catch (NumberFormatException e) {
                    // Skip invalid size
                }
            }
        }
        return sizes;
    }
}
