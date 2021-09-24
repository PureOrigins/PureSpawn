package it.pureorigins.randomspawn

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.stat.Stats
import net.minecraft.util.math.BlockPos
import kotlin.random.Random

object RandomSpawn : ModInitializer {

    private const val RANGE = 10000

    override fun onInitialize() {
        ServerPlayConnectionEvents.JOIN.register { handler: ServerPlayNetworkHandler, _: PacketSender, _: MinecraftServer ->
            if (handler.player.statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME)) == 0) {
                val x = Random.nextInt(RANGE)
                val y = Random.nextInt(RANGE)
                //TODO safety check, we care about our players health
                val block = handler.player.world.getBlockEntity(BlockPos(x,y,64))
                handler.player.teleport(handler.player.serverWorld, x.toDouble(), y.toDouble(),64.0, 0.0F,0.0F)
            }
        }
    }
}
