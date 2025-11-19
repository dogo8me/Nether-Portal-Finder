package com.dogo8me.findportal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PortalRegistry {
    private static final List<PortalData> portals = new ArrayList<>();
    private static final ReadWriteLock lock = new ReentrantReadWriteLock();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static Path registryFile;
    
    public static void init() {
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve("findportal");
        try {
            Files.createDirectories(configDir);
        } catch (IOException e) {
            System.err.println("Failed to create findportal config directory: " + e.getMessage());
        }
        registryFile = configDir.resolve("portals.json");
        load();
    }
    
    private static void load() {
        lock.writeLock().lock();
        try {
            if (Files.exists(registryFile)) {
                String json = Files.readString(registryFile);
                List<PortalData> loaded = gson.fromJson(json, new TypeToken<List<PortalData>>(){}.getType());
                if (loaded != null) {
                    portals.clear();
                    portals.addAll(loaded);
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load portal registry: " + e.getMessage());
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    private static void save() {
        lock.readLock().lock();
        try {
            String json = gson.toJson(portals);
            Files.writeString(registryFile, json);
        } catch (IOException e) {
            System.err.println("Failed to save portal registry: " + e.getMessage());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public static void addOrUpdate(PortalData portal) {
        lock.writeLock().lock();
        try {
            // Find existing portal at same location
            PortalData existing = null;
            for (PortalData p : portals) {
                if (p.matches(portal.dimension, portal.x, portal.y, portal.z, portal.width, portal.height)) {
                    existing = p;
                    break;
                }
            }
            
            if (existing != null) {
                existing.lit = portal.lit;
                existing.lastSeen = portal.lastSeen;
            } else {
                portals.add(portal);
            }
            save();
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public static PortalData findNearest(String dimension, double x, double y, double z, int pruneDays) {
        lock.readLock().lock();
        try {
            long cutoffTime = System.currentTimeMillis() - (pruneDays * 24L * 60L * 60L * 1000L);
            PortalData nearest = null;
            double minDist = Double.MAX_VALUE;
            
            for (PortalData p : portals) {
                if (!p.dimension.equals(dimension)) continue;
                if (p.lastSeen < cutoffTime) continue;
                
                double dist = p.distanceTo(x, y, z);
                if (dist < minDist) {
                    minDist = dist;
                    nearest = p;
                }
            }
            
            return nearest;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public static void pruneOld(int pruneDays) {
        lock.writeLock().lock();
        try {
            long cutoffTime = System.currentTimeMillis() - (pruneDays * 24L * 60L * 60L * 1000L);
            portals.removeIf(p -> p.lastSeen < cutoffTime);
            save();
        } finally {
            lock.writeLock().unlock();
        }
    }
}
