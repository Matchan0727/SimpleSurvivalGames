package jp.simplespace.matchan.simpleSurvivalGames.simplehardcore

import jp.simplespace.simplehardcore.HardcoreGame
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.World
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.*
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerJoinEvent
import java.util.*

class HardcoreListener(val game: HardcoreGame) : Listener {
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        if (!game.config.contains(player.uniqueId.toString())) {
            game.config.set(player.uniqueId.toString(), 0)
            game.saveConfig()
        }
        updateBoards()
    }

    fun updateBoards() {
        for (player in Bukkit.getOnlinePlayers()) {
            game.objective!!.getScore(player)
                .setScore(game.config.getInt(player.uniqueId.toString()))
        }
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val player = event.player
        if (player.gameMode == GameMode.CREATIVE || player.gameMode == GameMode.SPECTATOR) return
        Bukkit.getLogger().info("dead " + player.name)
        game.config.set(
            player.uniqueId.toString(),
            game.config.getInt(player.uniqueId.toString()) + 1
        )
        val source = event.damageSource
        if (source.sourceLocation != null && source.sourceLocation!!.world.environment == World.Environment.THE_END) {
            game.saveConfig()
            return
        }
        game.config.set(
            "worldcount",
            game.config.getInt("worldcount", 1) + 1
        )
        game.saveConfig()
        for (p in Bukkit.getOnlinePlayers()) {
            p.kick(
                Component.text(
                    """
    ${player.name}が死んでしまったので、
    ゲーム終了です！
    死因: 
    """.trimIndent()
                ).append(
                    event.deathMessage()!!
                ).appendNewline()
                    .append(Component.text("ワールド経過時間: "))
                    .append(Component.text(game.scheduler!!.timer))
            )
        }
        Bukkit.getServer().shutdown()
    }

    @EventHandler
    fun onDamage(event: EntityDamageEvent) {
        if (event.entity !is Player) return
        var player = event.entity as Player
        if (event.cause == EntityDamageEvent.DamageCause.ENTITY_ATTACK || event.cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION || event.cause == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) {
            return
        }
        if (event.cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION || event.cause == EntityDamageEvent.DamageCause.FALLING_BLOCK) return
        Bukkit.broadcast(Component.text(player.getName() + "は" + event.cause.name + "が原因で" + event.finalDamage.toInt() + "ダメージくらって、残りHPは" + (player.getHealth() - event.finalDamage).toInt() + "です！"))
    }

    @EventHandler
    fun onDamageByEntity(event: EntityDamageByEntityEvent) {
        if (event.entity is Player) {
            var player = event.entity as Player
            Bukkit.broadcast(Component.text(player.getName() + "は" + event.damager.name + "によって" + event.finalDamage.toInt() + "ダメージくらって、残りHPは" + (player.getHealth() - event.finalDamage).toInt() + "です！"))
        }
    }

    @EventHandler
    fun onEntityDeath(event: EntityDeathEvent) {
        if (event.entityType == EntityType.ENDER_DRAGON) {
            for (player in Bukkit.getOnlinePlayers()) {
                player.gameMode = GameMode.CREATIVE
            }
        }
    }

    @EventHandler
    fun onDamageByBlock(event: EntityDamageByBlockEvent) {
        if (event.entity !is Player) return
        var player = event.entity as Player
        Bukkit.broadcast(Component.text(player.getName() + "は" + event.damager!!.type.name + "で" + event.finalDamage.toInt() + "ダメージくらって、残りHPは" + (player.getHealth() - event.finalDamage).toInt() + "です！"))
    }

    @EventHandler
    fun onEntitySpawn(event: EntitySpawnEvent) {
        val entity = event.entity
        when (entity.type) {
            EntityType.WOLF -> entity.customName(Component.text("笹倉千代子").color(TextColor.color(18,166,8)))
            else -> {}
        }
    }

    fun onPlayerInteract(event: PlayerInteractEntityEvent) {
        if (event.rightClicked !is Player) return
        var target = event.rightClicked as Player
        //sasagusa以外だったらreturn
        if (target.getUniqueId() != UUID.fromString("df710bee-1179-4433-b892-cd9ce7d27eda")) return
        when (Random().nextInt(8)) {
            1 -> target.chat("ここはどこだ！")
            2 -> target.chat("チ◯コ生えてるチ◯コマンだ！")
            3 -> target.chat("まりおっ◯い")
            4 -> target.chat("ワンワンワンワン")
            5 -> target.chat("シコシコシコシコシコランド！")
            6 -> target.chat("出してねえやつは笑うな！")
            7 -> target.chat("過酷なオ◯ニーして精◯出してんのか！")
            else -> {
                target.chat("全く前が見えない！")
                target.chat("ここはどこだ！")
                target.chat("チ◯コ生えてるチ◯コマンだ！")
                target.chat("まりおっ◯い")
                target.chat("ワンワンワンワン")
                target.chat("シコシコシコシコシコランド！")
                target.chat("出してねえやつは笑うな！")
                target.chat("過酷なオ◯ニーして精◯出してんのか！")
            }
        }
    }
}
