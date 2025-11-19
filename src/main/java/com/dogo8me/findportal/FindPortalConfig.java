package com.dogo8me.findportal;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.Arrays;
import java.util.List;

@Config(name = "findportal")
public class FindPortalConfig implements ConfigData {
    
    @ConfigEntry.Gui.Tooltip
    public boolean showUnlitPortals = true;
    
    @ConfigEntry.Gui.Tooltip
    public List<String> validPortalSizes = Arrays.asList("2x3", "3x3", "4x5", "23x23");
    
    @ConfigEntry.Gui.Tooltip
    public int pruneDays = 30;
    
    @ConfigEntry.Gui.Tooltip
    public int maxScanRadius = 256;
}
