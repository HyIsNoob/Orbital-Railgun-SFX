# TODO

## Dynamic Sound Effects

- [ ] Add reverb or echo effects for railgun sounds in specific environments (e.g., caves, open fields, underwater).
- [ ] Implement directional sounds to make the sound louder or quieter based on the player's position relative to the firing location.

## Customizable Sound Settings

- [x] Add a config file to adjust the volume of individual sounds (e.g., scope-on, railgun shoot).
- [x] Allow players to enable or disable specific sound effects.

## Multiplayer Enhancements

- [ ] Implement positional audio for multiplayer, so players hear the railgun firing from the correct direction and distance.

## Advanced Sound Synchronization

- [ ] **Client-side sound position synchronization** - When players re-enter the sound range, the sound should continue from the current playback position instead of restarting from the beginning.
  - **Current Status**: Timestamp tracking is implemented server-side. The system correctly:
    - ✅ Tracks when railgun fires (millisecond precision)
    - ✅ Calculates elapsed time when players enter/re-enter the zone
    - ✅ Prevents expired sounds from playing (after 53 seconds)
    - ✅ Logs playback offset for debugging
  - **What's Needed**:
    1. Create custom `SoundInstance` class on client-side to track playback state
    2. Implement lower-level audio control using Minecraft's sound engine internals
    3. Add packet to send elapsed time from server to client
    4. Implement audio seeking to start playback at specific position (offset)
    5. Handle edge cases (sound instance cleanup, multiple laser impacts, etc.)
  - **Technical Challenge**: Minecraft's native `player.playSound()` API doesn't support starting sounds at arbitrary positions. This requires direct manipulation of the audio system.
  - **References**: 
    - Sound duration: 52992ms (~53 seconds) from railgun-shoot.ogg
    - Current implementation in `OrbitalRailgunSounds.handleAreaStateChange()`
    - Timestamp tracking in `PlayerAreaListener.AreaState`

## Compatibility with Other Mods

- [x] Add compatibility with **Dynamic Surroundings** or **Sound Physics Remastered** to enhance the audio experience.

## Debugging or Testing Features

- [x] Add a debug mode that logs sound events to the console for testing sound propagation and range.
