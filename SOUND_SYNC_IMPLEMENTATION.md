# Client-Side Sound Position Synchronization

## Overview

This document describes the implementation of client-side sound position synchronization for the Orbital Railgun Sounds mod. This feature ensures that when players enter the sound range of a railgun mid-playback, they hear the sound from the correct position rather than from the beginning.

## Problem Statement

Previously, when a railgun fired and a player entered the sound range after the sound started playing, the sound would play from the beginning for that player. This caused desynchronization where different players heard the sound at different positions.

## Solution

We implemented a synchronization system that:
1. Tracks when railgun sounds start playing (server-side)
2. Calculates elapsed time when players enter the sound range
3. Sends this elapsed time to the client
4. Uses OpenAL to seek the audio to the correct position

## Architecture

### Server-Side Components

#### 1. Timestamp Tracking
- **Location**: `PlayerAreaListener.AreaState`
- **Purpose**: Stores the timestamp when a railgun fires
- **Implementation**: Uses `System.currentTimeMillis()` for millisecond precision

#### 2. Elapsed Time Calculation
- **Location**: `OrbitalRailgunSounds.handleAreaStateChange()`
- **Purpose**: Calculates how much time has elapsed since the sound started
- **Logic**: `currentTime - fireTimestamp`

#### 3. Packet Sending
- **Location**: `OrbitalRailgunSounds.playRailgunSoundToPlayer()`
- **Packet ID**: `PLAY_SOUND_WITH_OFFSET_PACKET_ID`
- **Data**: Sound ID, position (X, Z), elapsed time, volume, pitch

### Client-Side Components

#### 1. Custom Sound Instance
- **File**: `PositionedRailgunSoundInstance.java`
- **Purpose**: Tracks sound state and playback offset
- **Features**:
  - Stores the offset time
  - Manages sound lifecycle
  - Automatically stops after sound duration

#### 2. Synchronized Sound Manager
- **File**: `SynchronizedSoundManager.java`
- **Purpose**: Handles audio seeking and playback
- **Key Methods**:
  - `playSoundWithOffset()`: Main entry point for playing synchronized sounds
  - `seekSound()`: Uses reflection and OpenAL to seek audio

#### 3. Packet Handler
- **Location**: `OrbitalRailgunSoundsClient.onInitializeClient()`
- **Purpose**: Receives and processes synchronization packets
- **Action**: Delegates to `SynchronizedSoundManager`

## Technical Implementation

### Audio Seeking

The core challenge is that Minecraft's native API doesn't support starting sounds at arbitrary positions. We solve this using:

1. **Reflection**: Access Minecraft's internal sound system
   - Navigate: `SoundManager` → `SoundSystem` → `Sources`
   - Field names are version-specific (tested with Minecraft 1.20.1)

2. **OpenAL**: Use the underlying audio library
   - Get the OpenAL source ID for the playing sound
   - Use `AL10.alSourcef(sourceId, AL10.AL_SEC_OFFSET, offsetSeconds)` to seek

3. **Retry Logic**: Handle timing issues
   - Try up to 5 times with progressive delays (20ms, 40ms, 60ms, 80ms, 100ms)
   - This ensures the sound source is fully initialized before seeking

### Fallback Behavior

If audio seeking fails (e.g., due to reflection issues or OpenAL unavailability):
- The sound plays from the beginning
- The offset information is still tracked
- Logs indicate fallback behavior
- No error interrupts gameplay

## Flow Diagram

```
┌────────────────┐
│ Railgun Fires  │
└───────┬────────┘
        │
        ▼
┌────────────────────────────┐
│ Server: Record Timestamp   │
│ (PlayerAreaListener)       │
└───────┬────────────────────┘
        │
        ▼
┌────────────────────────────┐
│ Player Enters Sound Range  │
└───────┬────────────────────┘
        │
        ▼
┌────────────────────────────┐
│ Server: Calculate Elapsed  │
│ elapsed = now - timestamp  │
└───────┬────────────────────┘
        │
        ▼
┌────────────────────────────┐
│ Server: Send Packet        │
│ PLAY_SOUND_WITH_OFFSET     │
└───────┬────────────────────┘
        │
        ▼
┌────────────────────────────┐
│ Client: Receive Packet     │
│ (OrbitalRailgunSoundsClient)│
└───────┬────────────────────┘
        │
        ▼
┌────────────────────────────┐
│ Client: Play Sound         │
│ (SynchronizedSoundManager) │
└───────┬────────────────────┘
        │
        ▼
┌────────────────────────────┐
│ Client: Seek Audio         │
│ (OpenAL AL_SEC_OFFSET)     │
└───────┬────────────────────┘
        │
        ▼
┌────────────────────────────┐
│ Synchronized Playback!     │
└────────────────────────────┘
```

## Files Modified

### New Files
1. `src/client/java/io/github/hyisnoob/railgunsounds/client/sound/PositionedRailgunSoundInstance.java`
   - Custom sound instance with offset tracking
   
2. `src/client/java/io/github/hyisnoob/railgunsounds/client/sound/SynchronizedSoundManager.java`
   - Audio seeking and synchronization logic

### Modified Files
1. `src/main/java/io/github/hyisnoob/railgunsounds/OrbitalRailgunSounds.java`
   - Added `PLAY_SOUND_WITH_OFFSET_PACKET_ID` packet identifier
   - Modified `playRailgunSoundToPlayer()` to send offset packets
   - Added documentation

2. `src/client/java/io/github/hyisnoob/railgunsounds/client/OrbitalRailgunSoundsClient.java`
   - Added packet handler for `PLAY_SOUND_WITH_OFFSET_PACKET_ID`
   - Integrated `SynchronizedSoundManager`

3. `TODO.md`
   - Updated to mark sound synchronization as complete

## Configuration

No additional configuration is required. The feature works automatically and transparently.

## Debug Logging

When `ServerConfig.INSTANCE.isDebugMode()` is enabled, the system logs:
- When players enter/leave sound ranges
- Elapsed time calculations
- Packet sending events
- Audio seeking attempts and results
- Fallback behavior

## Compatibility

- **Minecraft Version**: 1.20.1
- **Required**: Fabric Loader, Fabric API, Orbital Railgun mod
- **Optional**: Sound Physics Remastered, Dynamic Surroundings
- **Dependencies**: LWJGL (for OpenAL support)

## Performance Considerations

- Minimal overhead: Reflection is used only during sound playback initialization
- Thread safety: Uses `ConcurrentHashMap` for tracking active sounds
- Cleanup: Automatic cleanup after sound duration to prevent memory leaks
- Graceful degradation: Falls back to normal playback if seeking fails

## Testing

To test the implementation:
1. Start a multiplayer server or use two clients
2. Fire the Orbital Railgun
3. Move one player away from and then back into the sound range
4. Observe that the sound continues from where it should be, not from the beginning
5. Check debug logs to verify synchronization is working

## Known Limitations

1. **Reflection Dependency**: Field names are version-specific and may change in future Minecraft versions
2. **OpenAL Required**: Audio seeking requires OpenAL support
3. **Graceful Degradation**: If seeking fails, sounds play from the beginning (still functional, just not synchronized)

## Future Improvements

Possible enhancements:
1. Support for additional Minecraft versions through version detection
2. Configurable option to disable synchronization if desired
3. Better error handling and user feedback
4. Support for other long-duration sounds in the mod

## Credits

- Implementation: GitHub Copilot
- Original Mod: HyIsNoob
- Feature Request: KingIronMan2011
