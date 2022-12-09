package love.chihuyu.commands

import dev.jorel.commandapi.CommandAPICommand
import love.chihuyu.datas.PermissionNodes

object CommandWEV {

    val main: CommandAPICommand = CommandAPICommand("worldeditvisualizer")
        .withAliases("wev", "wv")
        .withPermission(PermissionNodes.CMD_ROOT.node)
        .withSubcommands(
            WEVToggle.main
        )
}