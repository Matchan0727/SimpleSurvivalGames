package jp.simplespace.matchan.simpleSurvivalGames.simplespeedrunner

import jp.simplespace.matchan.simpleSurvivalGames.IGame
import jp.simplespace.matchan.simpleSurvivalGames.Utils
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.inventory.meta.CompassMeta
import org.bukkit.plugin.Plugin
import java.util.*

class SpeedRunGame(val plugin: Plugin) : IGame{
    var status: Status
        private set
    val scheduler: SpeedRunScheduler
    val config: FileConfiguration
    var runner: UUID? = null
        private set
    private val listener: Listener
    var nowCompassLocation: Location? = null
        private set
    private var taskId = 0
    var deathCount: Int
    var isPause: Boolean = false
        private set

    init {
        this.status = Status.STOP
        this.config = Utils.saveDefaultConfig("speedrun.yml", plugin)
        this.listener = SpeedRunListener(this)
        this.scheduler = SpeedRunScheduler(this)
        this.deathCount = 0
    }

    override fun init(): Boolean {
        return true
    }

    fun setRunner(player: Player) {
        runner = player.uniqueId
    }

    fun getRunner(): UUID? {
        return runner
    }

    fun ready(): Boolean {
        if (status == Status.RUNNING || status == Status.READY) {
            return false
        }
        this.status = Status.READY
        Bukkit.getPluginManager().registerEvents(listener, plugin)
        for (player in Bukkit.getOnlinePlayers()) {
            player.setHealth(player.getHealthScale())
            player.setFoodLevel(32)
            for (effect in player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType())
            }
            player.getInventory().clear()
            if (player.uniqueId != runner) player.inventory.setItem(8, ItemStack(Material.COMPASS))
        }
        Bukkit.getPlayer(runner!!)!!.world.time = 0
        Bukkit.broadcastMessage(
            """
                ${ChatColor.GOLD}まもなくゲームが開始されるなぁ、そうに決まってる。
                ランナーはハンターをパン！してスタートするんだなぁ。
                """.trimIndent()
        )
        return true
    }

    fun saveConfig() {
        Utils.saveConfig(config, "speedrun.yml", plugin)
    }

    fun start(): Boolean {
        if (status == Status.RUNNING) {
            return false
        }
        this.status = Status.RUNNING
        Bukkit.broadcastMessage(ChatColor.GOLD.toString() + "ゲームが開始されてしまった！イクｩｩｩｩｩ")
        setCompassTargets()
        scheduler.count = 0
        scheduler.compassCount = config.getInt("compassInterval", 10)
        deathCount = 0
        taskId = plugin.server.scheduler.scheduleSyncRepeatingTask(plugin, scheduler, 0L, 20L)
        return true
    }

    fun setCompassTargets() {
        nowCompassLocation = Bukkit.getPlayer(runner!!)!!.location
        for (player in Bukkit.getOnlinePlayers()) {
            player.compassTarget = nowCompassLocation!!
            val inventory: PlayerInventory = player.getInventory()
            for (i in 0..45) {
                val item: ItemStack = inventory.getItem(i) ?: continue
                if (item.getType() == Material.COMPASS) {
                    val meta: CompassMeta = item.getItemMeta() as CompassMeta
                    meta.setLodestone(nowCompassLocation)
                    meta.setLodestoneTracked(false)
                    item.setItemMeta(meta)
                }
            }
        }
    }

    fun resetCompassTargets() {
        for (player in Bukkit.getOnlinePlayers()) {
            player.setCompassTarget(player.getWorld().getSpawnLocation())
        }
    }

    fun stop(): Boolean {
        if (status == Status.STOP) {
            return false
        }
        this.status = Status.STOP
        HandlerList.unregisterAll(listener)
        resetCompassTargets()
        plugin.server.scheduler.cancelTask(taskId)
        Bukkit.broadcastMessage(ChatColor.GOLD.toString() + "ゲームが終了したなぁ。")
        return true
    }

    enum class Status {
        READY, RUNNING, STOP
    }

    fun isPause(boo: Boolean) {
        this.isPause = boo
    }

    override fun unload(): Boolean {
        stop()
        return true
    }

    override fun canUnload(): Boolean {
        return status == Status.STOP
    }
}
