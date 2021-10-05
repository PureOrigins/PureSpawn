package it.pureorigins.randomspawn

import com.mojang.brigadier.CommandDispatcher
import it.pureorigins.framework.configuration.configFile
import it.pureorigins.framework.configuration.json
import it.pureorigins.framework.configuration.readFileAs
import kotlinx.serialization.Serializable
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
import java.lang.Math.toRadians
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random


object RandomSpawn : ModInitializer {

    override fun onInitialize() {

        val config = json.readFileAs(configFile("randomspawn.json"), Config())

        ServerPlayConnectionEvents.JOIN.register { handler: ServerPlayNetworkHandler, _: PacketSender, _: MinecraftServer ->
            val p = handler.player
            if (p.statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME)) == 0) {

                var x = 0
                var y = 64
                var z = 0
                var b = p.world.getBlockState(BlockPos(x, 0, z))

                while (b === Blocks.WATER) {
                    val radius = Random.nextInt(config.range)
                    val angle = toRadians(Random.nextDouble(360.0))
                    x = (radius * cos(angle)).roundToInt()
                    z = (radius * sin(angle)).roundToInt()
                    b = p.world.getBlockState(BlockPos(x, y, z))
                }
                while (b !== Blocks.AIR) b = p.world.getBlockState(BlockPos(x, ++y, z))
                p.setSpawnPoint(p.world.registryKey, BlockPos(x, y, z), 90F, true, false)
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
                    val radius = Random.nextInt(config.range)
                    val angle = toRadians(Random.nextDouble(360.0))
                    x = (radius * cos(angle)).roundToInt()
                    z = (radius * sin(angle)).roundToInt()
                    b = p.world.getBlockState(BlockPos(x, y, z))
                }
                while (b !== Blocks.AIR) b = p.world.getBlockState(BlockPos(x, ++y, z))
                p.setSpawnPoint(p.world.registryKey, BlockPos(x, y, z), 90F, true, false)
                1
            })
        })
    }

    @Serializable
    data class Config(
        val range: Int = 0
    )
}
