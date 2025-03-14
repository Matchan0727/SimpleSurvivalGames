package jp.simplespace.simplehardcore

import jp.simplespace.matchan.simpleSurvivalGames.IGame
import jp.simplespace.matchan.simpleSurvivalGames.SimpleSurvivalGames
import jp.simplespace.matchan.simpleSurvivalGames.Utils
import jp.simplespace.matchan.simpleSurvivalGames.hardcore.HardcoreListener
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.event.HandlerList
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Scoreboard
import org.codehaus.plexus.util.FileUtils
import java.io.File
import java.io.IOException

class HardcoreGame(val plugin: SimpleSurvivalGames) : IGame {

    val KEY = "simplehardcore"

    var reset = false
    val config: FileConfiguration
    var sb: Scoreboard? = null
    var scheduler: HardcoreScheduler? = null
    var cntObj: Objective? = null
    var hpobj: Objective? = null
    var taskId = 0
    var listener: HardcoreListener? = null

    init {
        this.config = Utils.saveDefaultConfig("hardcore.yml", plugin)
    }

    override fun init(): Boolean {
        reset = false
        setAutoSave(true)
        val server = plugin.server
        sb = server.scoreboardManager.mainScoreboard
        if (sb!!.getObjective("DeathCount") === null) {
            cntObj = sb!!.registerNewObjective("DeathCount", Criteria.DUMMY, Component.text("死亡回数"))
        } else cntObj = sb!!.getObjective("DeathCount")
        cntObj!!.displaySlot = DisplaySlot.SIDEBAR
        if (sb!!.getObjective("HP") === null) {
            hpobj = sb!!.registerNewObjective("HP", Criteria.HEALTH, MiniMessage.miniMessage().deserialize("<red>HP"))
        } else hpobj = sb!!.getObjective("HP")
        hpobj!!.displaySlot = DisplaySlot.BELOW_NAME
        hpobj!!.displaySlot = DisplaySlot.PLAYER_LIST
        scheduler = HardcoreScheduler(this)
        taskId = plugin.getServer().scheduler.scheduleSyncRepeatingTask(plugin, scheduler!!, 0L, 20L)
        if (server.getWorld("world") == null) {
            plugin.logger.warning("このプラグインを正常に機能させるためにワールドの名前は「world」にしてください。")
        }
        listener = HardcoreListener(this)
        server.pluginManager.registerEvents(listener!!, plugin)
        return true
    }

    fun saveConfig() {
        Utils.saveConfig(config, "hardcore.yml", plugin)
    }

    override fun unload(): Boolean {
        Utils.saveConfig(config, "hardcore.yml", plugin)
        cntObj!!.displaySlot = null
        hpobj!!.displaySlot = null
        cntObj!!.unregister()
        hpobj!!.unregister()
        plugin.logger.info(
            config.getInt("worldcount", 1).toString() + "代目ワールドの経過時間: " + scheduler?.let {
                it.timer(
                    it.count
                )
            }
        )
        if (Bukkit.getWorld("world") != null) {
            val container = Bukkit.getWorld("world")!!.persistentDataContainer
            scheduler?.count?.let { container.set(NamespacedKey(KEY, "count"), PersistentDataType.INTEGER, it) }
        }
        plugin.server.scheduler.cancelTask(taskId)
        HandlerList.unregisterAll(listener!!)
        return true
    }

    override fun canUnload(): Boolean {
        return true
    }

    fun setAutoSave(boo: Boolean) {
        Bukkit.getWorld("world")!!.isAutoSave = boo
        Bukkit.getWorld("world_nether")!!.isAutoSave = boo
        Bukkit.getWorld("world_the_end")!!.isAutoSave = boo
    }

    fun renameWorld(newWorldName: String) {
        val serverProperties = this.plugin.dataFolder.parentFile.parentFile.resolve("server.properties")

        try {
            val lines = serverProperties.readLines().toMutableList()
            for (i in lines.indices) {
                if (lines[i].startsWith("level-name=")) {
                    lines[i] = "level-name=$newWorldName"
                }
            }
            serverProperties.writeText(lines.joinToString("\n"))
            this.plugin.logger.info("server.properties を更新しました: level-name=$newWorldName")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
