package com.teampotato.isdpe;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
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
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.@NotNull PlayerLoggedInEvent event) {
        if (spawnDimension.get().equals("minecraft:overworld")) return;
        BlockPos.Mutable mutableBlockPos = new BlockPos.Mutable(spawnX.get(), spawnY.get(), spawnZ.get());
        PlayerEntity player = event.getPlayer();
        World world = player.level;
        if (world instanceof ServerWorld) {
            MinecraftServer server = ((ServerWorld)world).getServer();
            ServerWorld destination = server.getLevel(RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(spawnDimension.get())));
            if (destination == null) return;
            server.getCommands().performCommand(server.createCommandSourceStack().withSuppressedOutput(), "/execute in " + spawnDimension.get() + " run tp " + player.getStringUUID() + " " + spawnX + " " + spawnY + " " + spawnZ);
            if (allowSpawnUnderground.get()) {
                for (int i = spawnY.get(); i < 256; i ++) {
                    if (destination.getBlockState(mutableBlockPos).is(Blocks.AIR)) {
                        mutableBlockPos.setY(mutableBlockPos.getY() + 1);
                        if (destination.getBlockState(mutableBlockPos).is(Blocks.AIR)) {
                            player.setPos(player.getX(), i, player.getZ());
                            break;
                        }
                        mutableBlockPos.setY(mutableBlockPos.getY() - 1);
                    }
                }
            } else {
                for (int i = 0; i < 256; i ++) {
                    if (destination.getBlockState(mutableBlockPos).is(Blocks.AIR) && destination.canSeeSky(mutableBlockPos)) {
                        mutableBlockPos.setY(mutableBlockPos.getY() + 1);
                        if (destination.getBlockState(mutableBlockPos).is(Blocks.AIR)) {
                            player.setPos(player.getX(), i, player.getZ());
                            break;
                        }
                        mutableBlockPos.setY(mutableBlockPos.getY() - 1);
                    }
                }
            }
        }
    }
}
