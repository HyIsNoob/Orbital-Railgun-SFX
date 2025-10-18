# Audio Mod Compatibility

This mod is now compatible with popular audio enhancement mods for Minecraft.

## Compatible Mods

### Sound Physics Remastered
Sound Physics Remastered adds realistic sound physics to Minecraft, including:
- Reverb and echo effects based on environment
- Sound occlusion (sounds become muffled through walls)
- Directional audio
- Distance-based attenuation

**Compatibility Changes:**
- All sounds now use the `player` sound category for proper processing
- Attenuation distances are properly defined:
  - Railgun shoot: 64 blocks
  - Scope-on: 16 blocks
  - Equip: 8 blocks
- Scope sound uses LINEAR attenuation with proper spatial positioning

### Dynamic Surroundings
Dynamic Surroundings enhances the audio atmosphere with environmental effects.

**Compatibility Changes:**
- Sound category metadata added to all sound events
- Proper attenuation distances allow for environmental effects
- Sounds are positioned in 3D space for realistic audio

## How It Works

The mod now defines sound properties in `sounds.json` that these audio mods can process:

```json
{
  "railgun_shoot": {
    "sounds": [{
      "name": "orbital_railgun_sounds:railgun-shoot",
      "volume": 1.0,
      "pitch": 1.0,
      "attenuation_distance": 64
    }],
    "category": "player"
  }
}
```

These properties allow audio enhancement mods to:
1. Apply environmental effects (reverb, echo)
2. Calculate proper sound occlusion
3. Position sounds in 3D space
4. Apply distance-based volume falloff

## Installation

Simply install this mod alongside Sound Physics Remastered or Dynamic Surroundings. No additional configuration is required - the mods will automatically detect and enhance the railgun sounds.

## No Impact Without Audio Mods

If you don't have Sound Physics Remastered or Dynamic Surroundings installed, the sounds will work exactly as before. The compatibility changes are designed to be backward compatible and only enhance the audio when these mods are present.
