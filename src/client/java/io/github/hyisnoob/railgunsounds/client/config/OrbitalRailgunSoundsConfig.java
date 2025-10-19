package io.github.hyisnoob.railgunsounds.client.config;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;
import io.wispforest.owo.config.annotation.RangeConstraint;

@Modmenu(modId = "orbital_railgun_sounds")
@Config(name = "orbital-railgun-sounds", wrapperName = "OrbitalRailgunSoundsConfigWrapper")
public class OrbitalRailgunSoundsConfig {
    
    // Volume settings (0.0 to 1.0)
    @RangeConstraint(min = 0.0, max = 1.0)
    public double scopeVolume = 1.0;
    
    @RangeConstraint(min = 0.0, max = 1.0)
    public double shootVolume = 0.5;
    
    @RangeConstraint(min = 0.0, max = 1.0)
    public double equipVolume = 1.0;

    public boolean enableScopeSound = true;
    public boolean enableShootSound = true;
    public boolean enableEquipSound = true;
}
