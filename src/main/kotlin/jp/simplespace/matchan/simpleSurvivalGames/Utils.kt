package jp.simplespace.matchan.simpleSurvivalGames

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import java.io.File

class Utils {
    companion object {
        fun saveDefaultConfig(name: String, plugin: Plugin): FileConfiguration {
            if(!File(plugin.dataFolder,name).exists()) plugin.saveResource(name, false)
            return YamlConfiguration.loadConfiguration(File(plugin.dataFolder, name))
        }

        fun saveConfig(config: FileConfiguration, name: String, plugin: Plugin) {
            try {
                config.save(File(plugin.dataFolder, name))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}