package it.pureorigins.randomspawn

import com.mojang.brigadier.CommandDispatcher
import it.pureorigins.framework.configuration.configFile
import it.pureorigins.framework.configuration.json
import it.pureorigins.framework.configuration.readFileAs
import kotlinx.serialization.Serializable
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
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
import java.lang.Math.random
import java.util.*


object RandomSpawn : ModInitializer {

    private val logger = LogManager.getLogger()
    private var spawnBuffer = Stack<BlockPos>()

    //TODO avoid spawning on trees
    override fun onInitialize() {
        val config = json.readFileAs(configFile("randomspawn.json"), Config())
        logger.info("RandomSpawn successfully loaded!")
        ServerLifecycleEvents.SERVER_STARTED.register {

            for(i in 0..config.spawnBufferSize) {
                var pos: BlockPos?
                do {
                    val chunkPos = it.overworld.getChunk(randomPos(config.range, config.centerX, config.centerZ)).pos
                    pos = SpawnLocating.findServerSpawnPoint(it.overworld, chunkPos, true)
                    print("Spawn $i: $pos")
                } while (pos == null)
                spawnBuffer.push(pos)
            }
        }

        ServerPlayConnectionEvents.JOIN.register { handler: ServerPlayNetworkHandler, _: PacketSender, _: MinecraftServer ->
            val p = handler.player
            if (p.statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME)) == 0) {

                var pos: BlockPos?

                do {
                    val chunkPos = p.world.getChunk(randomPos(config.range, config.centerX, config.centerZ)).pos
                    pos = SpawnLocating.findServerSpawnPoint(p.serverWorld, chunkPos, true)
                } while (pos == null)
                p.teleport(pos!!.x + 0.5, pos!!.y.toDouble(), pos!!.z + 0.5)
                p.setSpawnPoint(p.world.registryKey, pos, 90F, true, false)
            }
        }

        CommandRegistrationCallback.EVENT.register(CommandRegistrationCallback { dispatcher: CommandDispatcher<ServerCommandSource?>, _: Boolean ->
            dispatcher.register(literal("test").executes { context ->
                val p = context.source.player
                var pos: BlockPos?

                do {
                    val chunkPos = p.world.getChunk(randomPos(config.range, config.centerX, config.centerZ)).pos
                    pos = SpawnLocating.findServerSpawnPoint(p.serverWorld, chunkPos, true)
                } while (pos == null)
                p.teleport(pos!!.x + 0.5, pos!!.y.toDouble(), pos!!.z + 0.5)
                p.setSpawnPoint(p.world.registryKey, pos, 90F, true, false)
                1
            })
        })

    }

    private fun randomPos(radius: Int, centerX: Int, centerZ: Int): BlockPos {
        var x: Double
        var y: Double
        do {
            x = (2 * random() - 1)
            y = (2 * random() - 1)
        } while (x * x + y * y > 1)
        return BlockPos((x * radius).toInt() + centerX, 64, (y * radius).toInt() + centerZ)
    }

    @Serializable
    data class Config(
        val range: Int = 25000,
        val centerX: Int = 0,
        val centerZ: Int = 0,
        val spawnBufferSize: Int = 10
    )
}
