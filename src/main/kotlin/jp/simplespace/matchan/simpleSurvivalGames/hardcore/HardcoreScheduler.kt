package jp.simplespace.simplehardcore

import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataType

class HardcoreScheduler : Runnable {
    var count = 0
    private var sec = 0
    private var min = 0
    private var hour = 0
    var timer = ""
        private set

    init {
        if (Bukkit.getWorld("world") != null) {
            val container = Bukkit.getWorld("world")!!.persistentDataContainer
            if (container.get<Int, Int>(
                    NamespacedKey(HardcoreGame.Companion.plugin!!, "count"),
                    PersistentDataType.INTEGER
                ) != null
            ) {
                count = container.get<Int, Int>(
                    NamespacedKey(HardcoreGame.Companion.plugin!!, "count"),
                    PersistentDataType.INTEGER
                )!!
            }
        }
    }

    override fun run() {
        count++
        timer = timer(count)
        for (player in Bukkit.getOnlinePlayers()) {
            if(player.isSleeping) continue
            player.spigot().sendMessage(
                ChatMessageType.ACTION_BAR,
                TextComponent(
                    ChatColor.GOLD.toString() + (HardcoreGame.Companion.config!!.getInt("worldcount", 1)
                        .toString() + "代目ワールドの経過時間: " + timer)
                )
            )
        }
    }

    fun timer(count: Int): String {
        if (count < 3600) {
            sec = count % 60
            min = count / 60
            hour = 0
        } else {
            sec = count % 60
            min = count / 60 % 60
            hour = count / 3600
        }
        return hour.toString() + "時間" + min + "分" + sec + "秒"
    }
}
