package com.teampotato.isdpe;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import static com.teampotato.isdpe.InitialSpawnDimensionPotatoEdition.*;

public class EventHandler {
    private static BlockPos.Mutable mutableBlockPos = null;

    private static void initSpawn(@NotNull MinecraftServer server, CommandSource commandSource, Commands commands, @NotNull PlayerEntity player, boolean safe) {
        float x = spawnX.get().floatValue();
        float y = spawnY.get().floatValue();
        float z = spawnZ.get().floatValue();
        BlockPos.Mutable mutable = new BlockPos.Mutable(x, y, z);
        String uuid = player.getStringUUID();
        String spawnDim = spawnDimension.get();
        ServerWorld destination = server.getLevel(RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(spawnDim)));
        if (destination == null) return;
        commands.performCommand(commandSource, "/execute in " + spawnDim + " run tp " + uuid + " " + x + " " + y + " " + z);
        if (allowSpawnUnderground.get()) {
            for (int i = (int)y; i < destination.getMaxBuildHeight(); i ++) {
                mutable.setY(i);
                BlockState below = destination.getBlockState(mutable.below());
                if (destination.getBlockState(mutable).is(Blocks.AIR) && destination.getBlockState(mutable.above()).is(Blocks.AIR) && !below.is(Blocks.AIR)) {
                    commands.performCommand(commandSource, "/execute in " + spawnDim + " run tp " + uuid + " " + x + " " + i + " " + z);
                    player.addTag("isdpe.spawned");
                    safe = true;
                    break;
                }
            }
        } else {
            for (int i = destination.getMaxBuildHeight(); i > (int)y; i --) {
                mutable.setY(i);
                BlockState below = destination.getBlockState(mutable.below());
                if (destination.getBlockState(mutable).is(Blocks.AIR) && destination.getBlockState(mutable.above()).is(Blocks.AIR) && !below.is(Blocks.AIR)) {
                    if (i > 100 && (below.is(BlockTags.LOGS) || below.is(BlockTags.LEAVES))) continue;
                    commands.performCommand(commandSource, "/execute in " + spawnDim + " run tp " + uuid + " " + x + " " + i + " " + z);
                    player.addTag("isdpe.spawned");
                    safe = true;
                    break;
                }
            }
        }
        if (!safe) {
            player.addTag("isdpe.spawned");
            destination.setBlockAndUpdate(mutableBlockPos, Blocks.STONE.defaultBlockState());
            commands.performCommand(commandSource, "/execute in " + spawnDim + " run tp " + uuid + " " + x + " 64 " + z);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.@NotNull PlayerLoggedInEvent event) {
        if (mutableBlockPos == null) mutableBlockPos = new BlockPos.Mutable(spawnX.get(), spawnY.get(), spawnZ.get());
        if (spawnDimension.get().equals("minecraft:overworld")) return;
        boolean safe = false;
        PlayerEntity player = event.getPlayer();
        World world = player.level;
        if (world instanceof ServerWorld && !player.getTags().contains("isdpe.spawned")) {
            MinecraftServer server = ((ServerWorld)world).getServer();
            CommandSource commandSource = server.createCommandSourceStack().withSuppressedOutput();
            Commands commands = server.getCommands();
            initSpawn(server, commandSource, commands, player, safe);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.@NotNull PlayerRespawnEvent event) {
        PlayerEntity player = event.getPlayer();
        World world = player.level;
        boolean safe = false;
        if (world instanceof ServerWorld && player instanceof ServerPlayerEntity) {
            if (((ServerPlayerEntity)player).getRespawnPosition() != null) return;
            MinecraftServer server = ((ServerWorld)world).getServer();
            CommandSource commandSource = server.createCommandSourceStack().withSuppressedOutput();
            Commands commands = server.getCommands();
            initSpawn(server, commandSource, commands, player, safe);
        }
    }
}
