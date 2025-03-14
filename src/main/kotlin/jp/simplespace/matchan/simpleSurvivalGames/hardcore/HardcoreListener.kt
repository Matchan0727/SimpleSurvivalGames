package jp.simplespace.matchan.simpleSurvivalGames.hardcore

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
import org.bukkit.event.player.PlayerJoinEvent

class HardcoreListener(val game: HardcoreGame) : Listener {

    init {
        updateBoards()
    }

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
            game.cntObj!!.getScore(player)
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
        val cnt = game.config.getInt("worldcount", 1) + 1
        game.config.set("worldcount", cnt)
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
        game.unload()
        game.renameWorld("world_$cnt")
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
            EntityType.WOLF -> entity.customName(Component.text("sasadog").color(TextColor.color(18,166,8)))
            else -> {}
        }
    }
}
