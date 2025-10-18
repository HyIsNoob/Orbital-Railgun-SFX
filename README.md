# Orbital Railgun – Sounds Addon

Client-side addon that adds custom sound effects to the [Orbital Railgun](https://modrinth.com/mod/orbital-railgun) mod. It introduces three core sounds—equip (draw), scope-on (start aiming), and fire (when the railgun actually shoots)

## Download (for players)

- [Go to Modrinth](https://modrinth.com/mod/orbital-railgun-sounds)

## How to Install

1. Make sure you’re on Minecraft 1.20.1 with Fabric Loader and Fabric API installed.
2. Install the original mod “Orbital Railgun” by Mishkis (required).
3. Drop this addon’s `.jar` into your `mods` folder.

This is client-side. You can join servers normally as long as your client has the original mod.

## Features

- Equip: plays when switching to the Orbital Railgun in your hotbar.
- Scope-on: plays when you begin aiming (hold right-click).
- Fire: plays when the railgun actually fires (at cooldown start), not simply on release.
- Customizable Settings: Adjust volume for each sound or enable/disable specific sound effects via the config menu (accessible through Mod Menu).

## Requirements

- Minecraft 1.20.1
- Fabric Loader + Fabric API
- Original mod: Orbital Railgun by [Mishkis](https://modrinth.com/user/Mishkis)
  - Mod page: <https://modrinth.com/mod/orbital-railgun>
- owo-lib (included in the mod)
- Mod Menu (optional, for easy config access)

## Configuration

Access the config screen through Mod Menu (if installed) to customize:
- Volume Settings: Adjust the volume for each sound (scope, shoot, equip) from 0.0 to 1.0
- Enable/Disable Sounds: Toggle individual sound effects on or off

Configuration is stored in `config/orbital-railgun-sounds.json5` in your Minecraft directory.

## Notes

- This addon does not change gameplay—only adds sound effects
- If you Alt-Tab, Minecraft will pause audio when unfocused while the animations is still going so it will be not sync anymore!

## Credits & License

- Original mod: Orbital Railgun by [Mishkis](https://modrinth.com/user/Mishkis)
- Sound assets: created by Me.
- Mod Improvements: KingIronMan2011
- Source code license: CC0 1.0 (see LICENSE)
