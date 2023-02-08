package love.chihuyu.listeners

import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.command.tool.InvalidToolBindException
import com.sk89q.worldedit.command.tool.brush.*
import com.sk89q.worldedit.math.Vector3
import com.sk89q.worldedit.regions.CylinderRegion
import com.sk89q.worldedit.regions.Region
import love.chihuyu.Plugin
import love.chihuyu.commands.WEPIgnoreAir
import love.chihuyu.commands.WEPToggle
import love.chihuyu.datas.ConfigKeys
import love.chihuyu.datas.PermissionNodes
import love.chihuyu.utils.runTaskLater
import org.bukkit.Location
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerMoveEvent

object PreviewBrush {

    private val cooltimed = mutableSetOf<Player>()
    private val previewBlocks = mutableMapOf<Player, MutableMap<Location, BlockData>>()

    @EventHandler
    fun moveCheck(e: PlayerMoveEvent) {
        val player = e.player

        if (!player.hasPermission(PermissionNodes.USE.node)
            || player.uniqueId !in WEPToggle.activatedPlayers
        ) return

        val latencied = Plugin.plugin.config.getInt(ConfigKeys.MOVE_CHECK_LATENCY.key)
        val session = WorldEdit.getInstance().sessionManager.getIfPresent(BukkitAdapter.adapt(player)) ?: return
        val brushTool = try { session.getBrushTool(BukkitAdapter.adapt(player.inventory.itemInMainHand).type) } catch (e: InvalidToolBindException) {
            previewBlocks[player]?.forEach {
                player.sendBlockChange(it.key, it.value)
            }
            previewBlocks[player] = mutableMapOf()
            return
        }
        val brush = session.getBrushTool(BukkitAdapter.adapt(player.inventory.itemInMainHand).type).brush

        fun showPrev() {
            val origin = player.rayTraceBlocks(Double.MAX_VALUE)?.hitBlock ?: return
            fun Region.regionPrev() {
                fun formatLoc(x: Double, y: Double, z: Double) = Location(
                    BukkitAdapter.adapt(session.selectionWorld),
                    origin.x - x + player.location.x,
                    origin.y - y + player.location.y,
                    origin.z - z + player.location.z
                )

                previewBlocks[player]?.forEach { player.sendBlockChange(it.key, it.value) }
                previewBlocks[player]?.clear()

                val preview = mutableMapOf<Location, BlockData>()
                this.forEach { block ->
                    val loc = formatLoc(block.x.toDouble(), block.y.toDouble(), block.z.toDouble())
                    val blockType = brushTool.material?.applyBlock(block)?.blockType ?: return@forEach
                    if (blockType.material?.isAir == true && player.uniqueId in WEPIgnoreAir.airIgnored) return@forEach
                    if (brushTool.mask.test(block)) player.sendBlockChange(loc, BukkitAdapter.adapt(blockType).createBlockData())
                    preview[loc] = player.world.getBlockData(loc)
                }

                previewBlocks[player] = preview
            }

            when (brush) {
                is ButcherBrush -> {}
                is ClipboardBrush -> {}
                is CylinderBrush -> {
                    CylinderRegion.createRadius(
                        session.createEditSession(BukkitAdapter.adapt(player)),
                        Vector3.at(origin.x.toDouble(), origin.y.toDouble(), origin.z.toDouble()).toBlockPoint(),
                        brushTool.size
                    ).regionPrev()
                }
                is GravityBrush -> {}
                is HollowCylinderBrush -> {}
                is HollowSphereBrush -> {}
                is ImageHeightmapBrush -> {}
                is OperationFactoryBrush -> {}
                is SmoothBrush -> {}
                is SphereBrush -> {}
            }
        }

        if (latencied > -1) {
            if (player in cooltimed) return
            cooltimed.add(player)
            Plugin.plugin.runTaskLater(latencied.toLong()) {
                showPrev()
                cooltimed.remove(player)
            }
        } else {
            showPrev()
        }
    }
}