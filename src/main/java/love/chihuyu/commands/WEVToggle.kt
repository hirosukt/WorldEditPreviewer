package love.chihuyu.commands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import love.chihuyu.Plugin.Companion.plugin
import love.chihuyu.Plugin.Companion.prefix
import love.chihuyu.datas.ConfigKeys
import org.bukkit.ChatColor
import java.util.*

object WEVToggle {

    val activatedPlayers = mutableSetOf<UUID>()

    val main: CommandAPICommand = CommandAPICommand("toggle")
        .executesPlayer(PlayerCommandExecutor { sender, _ ->
            val state = sender.uniqueId in activatedPlayers
            if (state) activatedPlayers.remove(sender.uniqueId) else activatedPlayers.add(sender.uniqueId)

            sender.sendMessage("$prefix Toggled previewing. ${ChatColor.GRAY}(${if (!state) "On" else "Off"})")

            plugin.config.set(ConfigKeys.ACTIVATED_PLAYERS.key, activatedPlayers)
            plugin.saveConfig()
        })
}