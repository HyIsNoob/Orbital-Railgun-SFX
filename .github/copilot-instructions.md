# Copilot Instructions for Orbital Railgun SFX

## Project Overview
This is a **Minecraft Fabric mod** that adds custom sound effects to the Orbital Railgun mod. It's a client-side addon that enhances the gameplay experience with custom audio for equipping, scoping, and firing the railgun.

## Technology Stack
- **Minecraft Version**: 1.20.1
- **Mod Loader**: Fabric Loader 0.17.2
- **Build Tool**: Gradle with Fabric Loom plugin
- **Java Version**: 17
- **Key Dependencies**:
  - Fabric API (0.92.6+1.20.1)
  - owo-lib (0.11.2+1.20) - for configuration UI
  - Gson (2.10.1) - for JSON handling
  - Yarn mappings for Minecraft deobfuscation

## Project Structure
```
src/
├── client/       # Client-side only code (sound handling, config UI)
│   └── java/io/github/hyisnoob/railgunsounds/client/
├── main/         # Common code (mod initialization, registries, networking)
│   ├── java/io/github/hyisnoob/railgunsounds/
│   └── resources/
│       ├── assets/orbital_railgun_sounds/
│       │   ├── sounds.json    # Sound event definitions
│       │   ├── sounds/        # Sound files (.ogg)
│       │   └── lang/          # Translations
│       └── fabric.mod.json    # Mod metadata
```

## Build System
- **Build Command**: `./gradlew build`
- **Clean Command**: `./gradlew clean`
- **Run Client**: Use a Minecraft launcher with Fabric
- **Configuration**: `gradle.properties` contains version information

## Code Style Guidelines
1. **Package Naming**: Use lowercase with dots: `io.github.hyisnoob.railgunsounds`
2. **Class Naming**: PascalCase (e.g., `OrbitalRailgunSounds`)
3. **Mod ID**: Always use `"orbital_railgun_sounds"` (constant in `OrbitalRailgunSounds.MOD_ID`)
4. **Resource Locations**: Use `Identifier` with the mod namespace
5. **Logging**: Use the project's Logger instance: `OrbitalRailgunSounds.LOGGER`

## Important Patterns
1. **Sound Events**: Register in `SoundsRegistry`, define in `sounds.json`
2. **Configuration**: Use owo-lib's config system (see `OrbitalRailgunSoundsConfig.java`)
3. **Client-Only Code**: Must be in the `client` source set to avoid server-side crashes
4. **Networking**: Use Fabric's networking API for client-server communication
5. **Split Source Sets**: The project uses Loom's `splitEnvironmentSourceSets()` feature

## Common Tasks
### Adding a New Sound
1. Place the `.ogg` file in `src/main/resources/assets/orbital_railgun_sounds/sounds/`
2. Register the sound event in `SoundsRegistry.java`
3. Add the sound definition to `sounds.json`
4. Add translations to language files in `lang/`

### Modifying Configuration
- Config class: `OrbitalRailgunSoundsConfig.java`
- Uses owo-lib's annotation-based config system
- Configuration file: `config/orbital-railgun-sounds.json5` (in user's Minecraft directory)

### Testing Changes
- This is a client-side mod, so test in a Minecraft client with Fabric
- Ensure the original Orbital Railgun mod is installed as a dependency
- Test with Mod Menu for config screen access

## Dependencies & Compatibility
- **Required**: Orbital Railgun mod by Mishkis
- **Optional but Recommended**: Mod Menu (for config access), Sound Physics Remastered, Dynamic Surroundings
- **Client-Side Only**: This mod can be used on servers without requiring server-side installation

## Licensing
- Source code is licensed under CC0 1.0 Universal (Public Domain)
- Original Orbital Railgun mod by Mishkis
- Sound assets created by HyIsNoob

## What NOT to Do
1. Don't add server-side logic to the client source set
2. Don't hardcode volumes or pitches - use the config system
3. Don't modify the original Orbital Railgun mod's behavior
4. Don't add gameplay changes - this is a sound effects addon only
5. Don't include copyrighted sound assets

## Useful References
- Fabric Wiki: https://fabricmc.net/wiki/
- owo-lib Documentation: https://docs.wispforest.io/owo/
- Minecraft Sound Events: Use Mojang's official mappings
