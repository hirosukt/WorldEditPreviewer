package love.chihuyu.listeners

import com.sk89q.worldedit.EmptyClipboardException
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import love.chihuyu.Plugin.Companion.plugin
import love.chihuyu.commands.WEVIgnoreAir
import love.chihuyu.commands.WEVToggle
import love.chihuyu.datas.ConfigKeys
import love.chihuyu.datas.PermissionNodes
import love.chihuyu.utils.runTaskLater
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

object PreviewClipboard : Listener {

    private val cooltimed = mutableSetOf<Player>()
    private val previewBlocks = mutableMapOf<Player, MutableMap<Location, BlockData>>()

    @EventHandler
    fun moveCheck(e: PlayerMoveEvent) {
        val player = e.player

        if (!player.hasPermission(PermissionNodes.USE.node)
            || player.uniqueId !in WEVToggle.activatedPlayers) return

        val latencied = plugin.config.getInt(ConfigKeys.MOVE_CHECK_LATENCY.key)
        val session = WorldEdit.getInstance().sessionManager.getIfPresent(BukkitAdapter.adapt(player)) ?: return
        val clipboardHolder = try { session.clipboard } catch (e: EmptyClipboardException) {
            previewBlocks[player]?.forEach {
                player.sendBlockChange(it.key, it.value)
            }
            previewBlocks[player] = mutableMapOf()
            return
        }
        val clipboard = clipboardHolder.clipboard ?: return

        fun showPrev() {
            val origin = clipboard.origin
            fun formatLoc(x: Double, y: Double, z: Double) = Location(
                BukkitAdapter.adapt(session.selectionWorld),
                player.location.x - (clipboardHolder.transform.apply(origin.toVector3()).x - x),
                player.location.y - (clipboardHolder.transform.apply(origin.toVector3()).y - y),
                player.location.z - (clipboardHolder.transform.apply(origin.toVector3()).z - z)
            )

            previewBlocks[player]?.forEach { player.sendBlockChange(it.key, it.value) }
            previewBlocks[player]?.clear()

            val preview = mutableMapOf<Location, BlockData>()
            clipboard.region.forEach { block ->
                val transformedBlock = clipboardHolder.transform.apply(block.toVector3())
                val loc = formatLoc(transformedBlock.x, transformedBlock.y, transformedBlock.z)
                val blockData = BukkitAdapter.adapt(clipboard.getBlock(block))

                if (blockData.material == Material.AIR && player.uniqueId in WEVIgnoreAir.airIgnored) return@forEach
                player.sendBlockChange(loc, blockData)
                preview[loc] = player.world.getBlockData(loc)
            }

            previewBlocks[player] = preview
        }

        if (latencied > -1) {
            if (player in cooltimed) return
            cooltimed.add(player)
            plugin.runTaskLater(latencied.toLong()) {
                showPrev()
                cooltimed.remove(player)
            }
        } else {
            showPrev()
        }
    }
}