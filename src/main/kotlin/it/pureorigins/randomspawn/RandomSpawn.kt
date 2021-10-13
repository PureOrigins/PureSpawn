package it.pureorigins.randomspawn

import com.mojang.brigadier.CommandDispatcher
import it.pureorigins.framework.configuration.configFile
import it.pureorigins.framework.configuration.json
import it.pureorigins.framework.configuration.readFileAs
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks.AIR
import net.minecraft.block.Material
import net.minecraft.server.MinecraftServer
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.stat.Stats
import net.minecraft.tag.ServerTagManagerHolder.getTagManager
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
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

        ServerPlayConnectionEvents.JOIN.register { handler: ServerPlayNetworkHandler, _: PacketSender, server: MinecraftServer ->
            val p = handler.player
            if (p.statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME)) == 0) {

                var x: Int
                val y = 64
                var z: Int
                var b: BlockState
                var pos: BlockPos

                do {
                    val radius = Random.nextInt(config.range)
                    val angle = toRadians(Random.nextDouble(360.0))
                    x = (radius * cos(angle)).roundToInt()
                    z = (radius * sin(angle)).roundToInt()
                    pos = BlockPos(x + config.centerX, y, z + config.centerZ)
                    b = p.world.getBlockState(pos)
                    logger.info("Selected block $x $y $z - ${b.block}")
                    while (!isSpaceOk(pos, p.world)) {
                        pos = pos.up()
                        b = p.world.getBlockState(pos)
                        logger.info("Selected upper block $x $y $z - ${b.block}")
                    }
                } while (!isGroundOk(pos, p.world))
                p.setSpawnPoint(p.world.registryKey, pos, 90F, true, false)
            }
        }

        CommandRegistrationCallback.EVENT.register(CommandRegistrationCallback { dispatcher: CommandDispatcher<ServerCommandSource?>, _: Boolean ->
            dispatcher.register(literal("test").executes { context ->
                val p = context.source.player
                var x: Int
                val y = 64
                var z: Int
                var b: BlockState
                var pos: BlockPos

                do {
                    val radius = Random.nextInt(config.range)
                    val angle = toRadians(Random.nextDouble(360.0))
                    x = (radius * cos(angle)).roundToInt()
                    z = (radius * sin(angle)).roundToInt()
                    pos = BlockPos(x + config.centerX, y, z + config.centerZ)
                    b = p.world.getBlockState(pos)
                    logger.info("Selected block $x $y $z - ${b.block}")
                    while (!isSpaceOk(pos, p.world)) {
                        pos = pos.up()
                        b = p.world.getBlockState(pos)
                        logger.info("Selected upper block $x $y $z - ${b.block}")
                    }
                } while (!isGroundOk(pos, p.world))
                p.setSpawnPoint(p.world.registryKey, pos, 90F, true, false)
                p.teleport(p.serverWorld, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), 0F, 0F)
                1
            })
        })
    }

    private fun isGroundOk(pos: BlockPos, w: World): Boolean {
        val ground = w.getBlockState(pos.down())
        val dirtTag = getTagManager().getTag(Registry.BLOCK_KEY, Identifier("dirt")) {
            SerializationException("Unknown block tag '$it'")
        }
        val sandTag = getTagManager().getTag(Registry.BLOCK_KEY, Identifier("sand")) {
            SerializationException("Unknown block tag '$it'")
        }
        if (ground.block in dirtTag || ground.block in sandTag) {
            logger.info("Selected block ${pos.x} ${pos.down().y} ${pos.z} is not a dangerous terrain (${ground.block})")
            return true
        }
        logger.info("Dangerous block: ${pos.x} ${pos.down().y} ${pos.z} is on the ${ground.block}")
        return false
    }

    private fun isSpaceOk(pos: BlockPos, w: World): Boolean {
        val head = w.getBlockState(pos.up())
        val legs = w.getBlockState(pos)
        if ((head.material === Material.REPLACEABLE_PLANT || head.block === AIR) && (legs.material === Material.REPLACEABLE_PLANT || legs.block === AIR)) {
            logger.info("Selected block ${pos.x} ${pos.y} ${pos.z} does not obstruct player")
            return true
        }
        logger.info("Selected block ${pos.x} ${pos.y} ${pos.z} is underground")
        logger.info("Head: ${head.block}\tLegs: ${legs.block}")
        return false
    }

    @Serializable
    data class Config(
        val range: Int = 0,
        val centerX: Int = 0,
        val centerZ: Int = 0
    )
}
