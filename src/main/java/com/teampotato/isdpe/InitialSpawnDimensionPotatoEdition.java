package com.teampotato.isdpe;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod("isdpe")
public class InitialSpawnDimensionPotatoEdition {
    public InitialSpawnDimensionPotatoEdition() {
        MinecraftForge.EVENT_BUS.register(EventHandler.class);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, config);
    }

    public static final ForgeConfigSpec config;
    public static final ForgeConfigSpec.IntValue spawnX, spawnY, spawnZ;
    public static final ForgeConfigSpec.ConfigValue<? extends String> spawnDimension;
    public static final ForgeConfigSpec.BooleanValue allowSpawnUnderground;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("InitialSpawnDimensionPotatoEdition");
        spawnDimension = builder.define("spawnDimension", "minecraft:overworld");
        allowSpawnUnderground = builder.comment("If the dimension you set has ceiling (Are you sure? This will be unsafe for players without pickaxes on spawn!), you should enable this").define("allowSpawnUnderground", false);
        spawnX = builder.defineInRange("spawnX", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
        spawnY = builder
                .comment("This value will be adjusted based on the situation.", "You wouldn't want players to spawn inside blocks, would you?", "If you want the player to spawn on normal ground, keep this at 0.")
                .defineInRange("spawnY", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
        spawnZ = builder.defineInRange("spawnZ", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
        builder.pop();
        config = builder.build();
    }
}
