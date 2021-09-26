package it.pureorigins.randomspawn

import com.mojang.brigadier.CommandDispatcher
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.block.Blocks
import net.minecraft.server.MinecraftServer
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.stat.Stats
import net.minecraft.util.math.BlockPos
import kotlin.random.Random


object RandomSpawn : ModInitializer {

    private const val RANGE = 10000

    override fun onInitialize() {
        ServerPlayConnectionEvents.JOIN.register { handler: ServerPlayNetworkHandler, _: PacketSender, _: MinecraftServer ->
            val p = handler.player
            if (p.statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME)) == 0) {

                var x = 0
                var y = 64
                var z = 0
                var b = p.world.getBlockState(BlockPos(x, 0, z))

                while (b === Blocks.WATER) {
                    x = Random.nextInt(RANGE)
                    z = Random.nextInt(RANGE)
                    b = p.world.getBlockState(BlockPos(x, y, z))
                }
                while(b !== Blocks.AIR) b = p.world.getBlockState(BlockPos(x, y++, z))
                p.teleport(p.serverWorld, x.toDouble(), y.toDouble(), z.toDouble(),0.0F,0.0F)
            }
        }

        CommandRegistrationCallback.EVENT.register(CommandRegistrationCallback { dispatcher: CommandDispatcher<ServerCommandSource?>, dedicated: Boolean ->
            dispatcher.register(literal("test").executes { context ->
                val p = context.source.player
                var x = 0
                var y = 64
                var z = 0
                var b = p.world.getBlockState(BlockPos(x, 0, z))

                while (b === Blocks.WATER) {
                    x = Random.nextInt(RANGE)
                    z = Random.nextInt(RANGE)
                    b = p.world.getBlockState(BlockPos(x, y, z))
                }
                while(b !== Blocks.AIR) b = p.world.getBlockState(BlockPos(x, y++, z))
                p.teleport(p.serverWorld, x.toDouble(), y.toDouble(), z.toDouble(),0.0F,0.0F)
                1
            })
        })
    }
}
