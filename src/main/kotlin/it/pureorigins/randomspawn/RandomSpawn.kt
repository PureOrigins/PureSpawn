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
import net.minecraft.server.MinecraftServer
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.server.network.SpawnLocating
import net.minecraft.stat.Stats
import net.minecraft.util.math.BlockPos
import org.apache.logging.log4j.LogManager
import java.lang.Math.toRadians
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random


object RandomSpawn : ModInitializer {

    private val logger = LogManager.getLogger()

    //TODO avoid spawning on trees
    override fun onInitialize() {
        val config = json.readFileAs(configFile("randomspawn.json"), Config())
        logger.info("RandomSpawn successfully loaded!")

        ServerPlayConnectionEvents.JOIN.register { handler: ServerPlayNetworkHandler, _: PacketSender, _: MinecraftServer ->
            val p = handler.player
            if (p.statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME)) == 0) {

                var x: Int
                val y = 64
                var z: Int
                var pos: BlockPos?

                do {
                    val radius = Random.nextInt(config.range)
                    val angle = toRadians(Random.nextDouble(360.0))
                    x = (radius * cos(angle)).roundToInt()
                    z = (radius * sin(angle)).roundToInt()
                    val chunkPos = p.world.getChunk(BlockPos(x + config.centerX, y, z + config.centerZ)).pos
                    pos = SpawnLocating.findServerSpawnPoint(p.serverWorld, chunkPos, true)
                } while (pos == null)
                p.teleport(pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5)
                p.setSpawnPoint(p.world.registryKey, pos, 90F, true, false)
            }
        }

        CommandRegistrationCallback.EVENT.register(CommandRegistrationCallback { dispatcher: CommandDispatcher<ServerCommandSource?>, _: Boolean ->
            dispatcher.register(literal("test").executes { context ->
                val p = context.source.player
                var x: Int
                val y = 64
                var z: Int
                var pos: BlockPos?

                do {
                    val radius = Random.nextInt(config.range)
                    val angle = toRadians(Random.nextDouble(360.0))
                    x = (radius * cos(angle)).roundToInt()
                    z = (radius * sin(angle)).roundToInt()
                    val chunkPos = p.world.getChunk(BlockPos(x + config.centerX, y, z + config.centerZ)).pos
                    pos = SpawnLocating.findServerSpawnPoint(p.serverWorld, chunkPos, true)
                } while (pos == null)
                p.teleport(pos!!.x + 0.5, pos!!.y.toDouble(), pos!!.z + 0.5)
                p.setSpawnPoint(p.world.registryKey, pos, 90F, true, false)
                1
            })
        })

    }

    @Serializable
    data class Config(
        val range: Int = 25000,
        val centerX: Int = 0,
        val centerZ: Int = 0
    )
}
