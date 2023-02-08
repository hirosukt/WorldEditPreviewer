package love.chihuyu.commands

import dev.jorel.commandapi.CommandAPICommand
import love.chihuyu.datas.PermissionNodes

object CommandWEV {

    val main: CommandAPICommand = CommandAPICommand("worldeditpreviewer")
        .withAliases("wep", "wp")
        .withPermission(PermissionNodes.CMD_ROOT.node)
        .withSubcommands(
            WEPToggle.main,
            WEPIgnoreAir.main
        )
}