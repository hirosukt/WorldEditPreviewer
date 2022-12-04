package love.chihuyu

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import love.chihuyu.listeners.PreviewClipboard
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

class Plugin : JavaPlugin(), Listener {

    companion object {
        lateinit var plugin: JavaPlugin
        lateinit var protocol: ProtocolManager
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
    }
}