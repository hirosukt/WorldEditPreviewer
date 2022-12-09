package love.chihuyu

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import love.chihuyu.commands.CommandWEV
import love.chihuyu.commands.WEVToggle
import love.chihuyu.datas.ConfigKeys
import love.chihuyu.listeners.PreviewClipboard
import org.bukkit.ChatColor
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

class Plugin : JavaPlugin(), Listener {

    companion object {
        lateinit var plugin: JavaPlugin
        lateinit var protocol: ProtocolManager
        val prefix = "${ChatColor.GOLD}[WEV]${ChatColor.RESET}"
    }

    init {
        plugin = this
    }

    override fun onLoad() {
        protocol = ProtocolLibrary.getProtocolManager()
    }

    override fun onEnable() {
        server.pluginManager.registerEvents(this, this)
        saveDefaultConfig()

        listOf(
            PreviewClipboard
        ).forEach {
            server.pluginManager.registerEvents(it, this)
        }

        CommandWEV.main.register()

        config.getList(ConfigKeys.ACTIVATED_PLAYERS.key)?.forEach {
            WEVToggle.activatedPlayers.add(UUID.fromString(it.toString()))
        }
    }
}