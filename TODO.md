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

- [x] **Client-side sound position synchronization** - When players re-enter the sound range, the sound should continue from the current playback position instead of restarting from the beginning.
  - **Implementation Status**: ✅ Complete
    - ✅ Tracks when railgun fires (millisecond precision)
    - ✅ Calculates elapsed time when players enter/re-enter the zone
    - ✅ Prevents expired sounds from playing (after 53 seconds)
    - ✅ Logs playback offset for debugging
    - ✅ Created custom `PositionedRailgunSoundInstance` class on client-side to track playback state
    - ✅ Implemented lower-level audio control using OpenAL (via reflection)
    - ✅ Added `PLAY_SOUND_WITH_OFFSET_PACKET_ID` packet to send elapsed time from server to client
    - ✅ Implemented audio seeking to start playback at specific position (offset)
    - ✅ Handle edge cases (sound instance cleanup, multiple laser impacts, etc.)
  - **Technical Implementation**: 
    - Uses OpenAL's `AL_SEC_OFFSET` to seek audio to the correct position
    - Falls back gracefully to playing from start if seeking is not available
    - Custom `SynchronizedSoundManager` handles all synchronized playback
  - **References**: 
    - Sound duration: 52992ms (~53 seconds) from railgun-shoot.ogg
    - Implementation in `OrbitalRailgunSounds.playRailgunSoundToPlayer()`
    - Client-side handler in `OrbitalRailgunSoundsClient`
    - Custom sound classes in `client.sound` package

## Compatibility with Other Mods

- [x] Add compatibility with **Dynamic Surroundings** or **Sound Physics Remastered** to enhance the audio experience.

## Debugging or Testing Features

- [x] Add a debug mode that logs sound events to the console for testing sound propagation and range.
