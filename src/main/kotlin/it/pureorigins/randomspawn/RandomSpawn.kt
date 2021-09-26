package it.pureorigins.randomspawn

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.block.Blocks
import net.minecraft.server.MinecraftServer
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
                var y = 0
                var z = 64
                var b = p.world.getBlockState(BlockPos(x, y, 0))

                while (b === Blocks.WATER) {
                    x = Random.nextInt(RANGE)
                    y = Random.nextInt(RANGE)
                    b = p.world.getBlockState(BlockPos(x, y, z))
                }
                while(b !== Blocks.AIR) b = p.world.getBlockState(BlockPos(x, y, z++))
                p.teleport(p.serverWorld, x.toDouble(), y.toDouble(), z.toDouble(),0.0F,0.0F)
            }
        }
    }
}
