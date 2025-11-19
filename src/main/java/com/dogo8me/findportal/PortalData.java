package com.dogo8me.findportal;

public class PortalData {
    public String dimension;
    public double x;
    public double y;
    public double z;
    public int width;
    public int height;
    public boolean lit;
    public long lastSeen;
    
    public PortalData() {
    }
    
    public PortalData(String dimension, double x, double y, double z, int width, int height, boolean lit, long lastSeen) {
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
        this.width = width;
        this.height = height;
        this.lit = lit;
        this.lastSeen = lastSeen;
    }
    
    public double distanceTo(double px, double py, double pz) {
        double dx = this.x - px;
        double dy = this.y - py;
        double dz = this.z - pz;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
    
    public boolean matches(String dim, double px, double py, double pz, int w, int h) {
        return this.dimension.equals(dim)
            && Math.abs(this.x - px) < 1.0
            && Math.abs(this.y - py) < 1.0
            && Math.abs(this.z - pz) < 1.0
            && this.width == w
            && this.height == h;
    }
}
