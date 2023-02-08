package love.chihuyu.commands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import love.chihuyu.Plugin.Companion.plugin
import love.chihuyu.Plugin.Companion.prefix
import love.chihuyu.datas.ConfigKeys
import org.bukkit.ChatColor
import java.util.*

object WEPIgnoreAir {

    val airIgnored = mutableSetOf<UUID>()

    val main: CommandAPICommand = CommandAPICommand("ignoreAir")
        .executesPlayer(PlayerCommandExecutor { sender, args ->
            val state = sender.uniqueId in airIgnored
            if (state) airIgnored.remove(sender.uniqueId) else airIgnored.add(sender.uniqueId)

            sender.sendMessage("$prefix Toggled previewing Air. ${ChatColor.GRAY}(${if (!state) "On" else "Off"})")

            plugin.config.set(ConfigKeys.AIR_IGNORED_PLAYERS.key, airIgnored.map { it.toString() })
            plugin.saveConfig()
        })
}