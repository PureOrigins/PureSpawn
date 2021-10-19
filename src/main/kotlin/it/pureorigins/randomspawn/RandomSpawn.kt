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
import net.minecraft.server.world.ServerWorld
import net.minecraft.stat.Stats
import net.minecraft.util.math.BlockPos
import org.apache.logging.log4j.LogManager
import java.lang.Math.random
import java.util.*


object RandomSpawn : ModInitializer {

    private val logger = LogManager.getLogger()
    private lateinit var spawnBuffer: SpawnBuffer

    //TODO avoid spawning on trees
    override fun onInitialize() {
        val config = json.readFileAs(configFile("randomspawn.json"), Config())
        logger.info("RandomSpawn successfully loaded!")
        ServerLifecycleEvents.SERVER_STARTED.register {
            spawnBuffer =
                SpawnBuffer(it.overworld, config.range, config.centerX, config.centerZ, config.spawnBufferSize)
        }

        ServerPlayConnectionEvents.JOIN.register { handler: ServerPlayNetworkHandler, _: PacketSender, _: MinecraftServer ->
            val p = handler.player
            if (p.statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME)) == 0) {
                val pos = spawnBuffer.pop()
                p.teleport(pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5)
                p.setSpawnPoint(p.world.registryKey, pos, 90F, true, false)
                spawnBuffer.addSpawnPoint()
            }
        }

        CommandRegistrationCallback.EVENT.register(CommandRegistrationCallback { dispatcher: CommandDispatcher<ServerCommandSource?>, _: Boolean ->
            dispatcher.register(literal("test").executes { context ->
                val p = context.source.player
                val pos = spawnBuffer.pop()
                p.teleport(pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5)
                p.setSpawnPoint(p.world.registryKey, pos, 90F, true, false)
                spawnBuffer.addSpawnPoint()
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

    class SpawnBuffer(
        private val overworld: ServerWorld,
        private val range: Int,
        private val centerX: Int,
        private val centerZ: Int,
        size: Int
    ) : Stack<BlockPos>() {
        init {
            for (i in 0..size) this.push(findSpawnPoint())
        }

        override fun pop(): BlockPos = if (empty())
            findSpawnPoint()
        else super.pop()

        fun addSpawnPoint() {
            push(findSpawnPoint())
        }

        private fun findSpawnPoint(): BlockPos {
            var pos: BlockPos?
            do {
                val chunkPos = overworld.getChunk(randomPos(range, centerX, centerZ)).pos
                pos = SpawnLocating.findServerSpawnPoint(overworld, chunkPos, true)
            } while (pos == null)
            return pos
        }
    }
}
