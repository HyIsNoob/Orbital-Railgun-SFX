# Orbital Railgun – Sounds Addon

Client-side addon that adds custom sound effects to the [Orbital Railgun](https://modrinth.com/mod/orbital-railgun) mod. It introduces three core sounds—equip (draw), scope-on (start aiming), and fire (when the railgun actually shoots)—and aligns playback with the item’s real usage and cooldown so audio matches animations. When the game window loses focus (Alt+Tab), sounds continue “silently” (volume = 0) to keep the audio timeline in sync with animations, reducing desync when you return.

## Download (for players)

- Go to the GitHub Releases tab of this repo and download the latest `.jar` file.
- Link: <https://github.com/HyIsNoob/Orbital-Railgun-SFX/releases>

## How to Install

1) Make sure you’re on Minecraft 1.20.1 with Fabric Loader and Fabric API installed.
2) Install the original mod “Orbital Railgun” by Mishkis (required).
3) Drop this addon’s `.jar` into your `mods` folder.

This is client-side. You can join servers normally as long as your client has the original mod.

## Features

- Equip: plays when switching to the Orbital Railgun in your hotbar.
- Scope-on: plays when you begin aiming (hold right-click).
- Fire: plays when the railgun actually fires (at cooldown start), not simply on release.
- Alt‑tab handling: audio continues muted while the game is unfocused to preserve timing with animations.

## Requirements

- Minecraft 1.20.1
- Fabric Loader + Fabric API
- Original mod: Orbital Railgun by [Mishkis](https://modrinth.com/user/Mishkis)
  - Mod page: <https://modrinth.com/mod/orbital-railgun>

## Notes

- This addon does not change gameplay—only adds sound effects and simple synchronization logic.
- Depending on system/driver settings, some environments may still pause audio when unfocused; the addon mitigates desync by continuing playback at zero volume whenever possible.

## Credits & License

- Original mod: Orbital Railgun by [Mishkis](https://modrinth.com/user/Mishkis)
- Sound assets: created by the author of this addon.
- Source code license: CC0 1.0 (see LICENSE)
