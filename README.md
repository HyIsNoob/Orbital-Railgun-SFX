# Orbital Railgun – Sounds Addon

A lightweight client-side addon that adds custom sound effects to the [Orbital Railgun](https://modrinth.com/mod/orbital-railgun) mod. It introduces three core sounds—equip (draw), scope-on (start aiming), and fire (when the railgun actually shoots)—and aligns playback with the item’s real usage and cooldown so audio matches animations. When the game window loses focus (alt‑tab), sounds continue “silently” (volume = 0) to keep the audio timeline in sync with animations, reducing desync when you return.

## Features

- Equip: plays when switching to the Orbital Railgun in the hotbar.
- Scope-on: plays when you begin aiming (hold right-click).
- Fire: plays only when the railgun truly fires (at cooldown start), not simply on release.
- Alt‑tab handling: audio continues muted while unfocused to preserve timing with animations.

## Requirements

- Minecraft 1.20.1
- Fabric Loader + Fabric API
- Original mod: Orbital Railgun by [Mishkis](https://modrinth.com/user/Mishkis)  
	Mod page: <https://modrinth.com/mod/orbital-railgun>

## Installation

1. Download the addon JAR from the Releases or your build output (`build/libs`).
2. Place the JAR in your `mods` folder alongside Orbital Railgun and Fabric API.
3. This is a client-side addon; you can join servers as long as your client has the original mod installed.

## Notes

- This addon does not change gameplay—only adds sound effects and simple synchronization logic.
- Depending on system/driver settings, some environments may still pause audio when unfocused; the addon mitigates desync by continuing playback at zero volume whenever possible.

## Credits

- Original mod: Orbital Railgun by [Mishkis](https://modrinth.com/user/Mishkis)
- Sounds addon: community-made, unofficial extension focused on audio and sync improvements
