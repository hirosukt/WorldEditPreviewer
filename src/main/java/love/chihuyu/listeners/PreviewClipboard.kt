package love.chihuyu.listeners

import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.bukkit.BukkitWorld
import com.sk89q.worldedit.world.World
import love.chihuyu.ConfigKeys
import love.chihuyu.Plugin.Companion.plugin
import love.chihuyu.utils.runTaskLater
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

object PreviewClipboard : Listener {

    private val cooltimed = mutableSetOf<Player>()

    private val previewedBlocks = mutableMapOf<Player, List<Location>>()

    @EventHandler
    fun moveCheck(e: PlayerMoveEvent) {
        val latencied = plugin.config.getInt(ConfigKeys.MOVE_CHECK_LATENCY.key)
        val player = e.player
        val session = WorldEdit.getInstance().sessionManager.getIfPresent(BukkitAdapter.adapt(player)) ?: return
        val clipboardHolder = session.clipboard ?: return
        val clipboard = clipboardHolder.clipboard ?: return

        fun showPrev() {
            fun formatLoc(x: Int, y: Int, z: Int) = Location(
                BukkitAdapter.adapt(session.selectionWorld),
                player.location.x - (clipboard.origin.x - x),
                player.location.y - (clipboard.origin.y - y),
                player.location.z - (clipboard.origin.z - z)
            )

            previewedBlocks[player]?.forEach {
                player.sendBlockChange(it, BukkitAdapter.adapt(session.selectionWorld).getBlockAt(it).blockData)
            }

            clipboard.region.forEach {
                player.sendBlockChange(
                    formatLoc(it.x, it.y, it.z),
                    BukkitAdapter.adapt(clipboard.getBlock(it))
                )
            }

            previewedBlocks[player] = clipboard.region.map { formatLoc(it.x, it.y, it.z) }
        }

        if (latencied > 0) {
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