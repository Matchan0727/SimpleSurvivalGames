package jp.simplespace.matchan.simpleSurvivalGames.speedrun

import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.configuration.file.FileConfiguration

class SpeedRunScheduler(val game: SpeedRunGame) : Runnable {
    var count: Int = 0
    var compassCount: Int
    private val config: FileConfiguration = game.config

    init {
        compassCount = config.getInt("compassInterval", 10)
    }

    override fun run() {
        if (game.isPause) {
            for (player in Bukkit.getOnlinePlayers()) {
                player.spigot()
                    .sendMessage(ChatMessageType.ACTION_BAR, TextComponent(ChatColor.GOLD.toString() + "一時停止中..."))
            }
            return
        }
        var hasJoin = false
        for (player in Bukkit.getOnlinePlayers()) {
            if (player.getUniqueId() == game.runner) {
                hasJoin = true
                break
            }
        }
        if (!hasJoin) {
            game.isPause(true)
            Bukkit.broadcastMessage(ChatColor.GOLD.toString() + "ランナーが退出したためゲームを一時停止させました。")
            return
        }
        count++
        val min = count / 60
        val sec = count % 60
        val timer = min.toString() + "分" + sec + "秒"
        for (player in Bukkit.getOnlinePlayers()) {
            player.spigot().sendMessage(
                ChatMessageType.ACTION_BAR,
                TextComponent(
                    ChatColor.GOLD.toString() + "経過時間: " + timer + "     コンパス更新まで: " + compassCount + "秒     ランナー死亡回数: " + game.deathCount + "/" + config.getInt(
                        "maxDeath",
                        5
                    ) + "回"
                )
            )
        }
        compassCount--
        if (compassCount <= 0) {
            game.setCompassTargets()
            if (config.getBoolean(
                    "compassAnnounce",
                    false
                )
            ) Bukkit.broadcastMessage(ChatColor.GOLD.toString() + "コンパスの座標が更新されました！")
            compassCount = config.getInt("compassInterval", 10)
        }
    }
}
