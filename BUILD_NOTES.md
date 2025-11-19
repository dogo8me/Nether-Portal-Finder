# Build Notes

## Build Requirements

This Fabric mod project requires:
- Java 17 or later
- Gradle 8.8
- Access to maven.fabricmc.net for Fabric dependencies
- Access to maven.shedaniel.me for Cloth Config

## Project Structure

```
findportal/
├── build.gradle                 # Gradle build configuration
├── settings.gradle              # Gradle settings  
├── gradle.properties            # Build properties
├── gradle/wrapper/              # Gradle wrapper
├── src/
│   └── main/
│       ├── java/com/dogo8me/findportal/
│       │   ├── FindPortalMod.java       # Main mod entry point
│       │   ├── FindPortalConfig.java    # AutoConfig configuration
│       │   ├── FindPortalCommand.java   # /findportal command handler
│       │   ├── PortalData.java          # Portal data model
│       │   ├── PortalRegistry.java      # JSON persistence layer
│       │   └── PortalScanner.java       # World scanning logic
│       └── resources/
│           ├── fabric.mod.json          # Mod metadata
│           └── pack.mcmeta              # Resource pack metadata
```

## Dependencies

- Minecraft 1.21.1
- Fabric Loader 0.16.9
- Fabric API 0.107.0+1.21.1
- Cloth Config (Fabric) 15.0.140

## Known Build Limitations

The build requires access to `maven.fabricmc.net` which may be blocked in some environments.
If you encounter DNS or network errors when building, ensure you have:
1. Internet connectivity
2. Access to maven.fabricmc.net (not blocked by firewall/DNS)
3. Access to maven.shedaniel.me (not blocked by firewall/DNS)

The mod will build successfully in a local development environment with proper network access.
