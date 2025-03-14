package jp.simplespace.matchan.simpleSurvivalGames

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.java.JavaPlugin

class SimpleSurvivalGames : JavaPlugin() {

    companion object {
        lateinit var config: FileConfiguration
    }

    private val logger = getLogger()
    private val server = getServer()
    private val pluginManager = server.pluginManager

    override fun onEnable() {
        // Plugin startup logic
        saveDefaultConfig()
        Companion.config = config
        GameSelector.instance = GameSelector(this)
        logger.info("enabled")
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
