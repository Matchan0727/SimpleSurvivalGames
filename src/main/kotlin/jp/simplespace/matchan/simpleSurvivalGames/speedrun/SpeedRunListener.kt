package jp.simplespace.matchan.simpleSurvivalGames.simplespeedrunner

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.inventory.ItemStack

class SpeedRunListener(val game: SpeedRunGame) : Listener {

    private val config = game.config

    @EventHandler
    fun onMove(event: PlayerMoveEvent) {
        if (game.isPause) {
            event.setCancelled(true)
        }
    }

    @EventHandler
    fun onReadyDamage(event: EntityDamageByEntityEvent) {
        if (game.status != SpeedRunGame.Status.READY) {
            return
        }
        if (!((event.getDamager() is Player) && (event.getEntity() is Player))) {
            return
        }
        val attacker: Player = event.getDamager() as Player
        val target: Player = event.getEntity() as Player
        if (attacker.getUniqueId() == game.runner) {
            game.start()
        }
    }

    @EventHandler
    fun onBreak(event: BlockBreakEvent) {
        if (game.status == SpeedRunGame.Status.READY) {
            event.setCancelled(true)
        }
    }

    @EventHandler
    fun onPlace(event: BlockPlaceEvent) {
        if (game.status == SpeedRunGame.Status.READY) {
            event.setCancelled(true)
        }
    }

    @EventHandler
    fun onDropItem(event: PlayerDropItemEvent) {
        if (game.status == SpeedRunGame.Status.READY) {
            event.setCancelled(true)
        }
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player: Player = event.getPlayer()
        if (game.status == SpeedRunGame.Status.RUNNING) {
            game.nowCompassLocation?.let { player.setCompassTarget(it) }
            if (!player.getInventory().contains(Material.COMPASS) && player.getUniqueId() != game.runner) {
                player.getInventory().setItem(8, ItemStack(Material.COMPASS))
            }
        }
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        val player: Player = event.getEntity()
        if (game.status == SpeedRunGame.Status.RUNNING) {
            if (player.getInventory().contains(Material.COMPASS)) {
                player.getInventory().remove(Material.COMPASS)
            }
        }
        if (player.getUniqueId() == game.runner) {
            game.deathCount = game.deathCount + 1
            if (game.deathCount >= config.getInt("maxDeath", 5)) {
                game.stop()
                for (p in Bukkit.getServer().getOnlinePlayers()) {
                    if (p.getUniqueId() == game.runner) p.playSound(
                        p.getLocation(),
                        Sound.ENTITY_LIGHTNING_BOLT_THUNDER,
                        1f,
                        1f
                    )
                    else p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
                }
                Bukkit.broadcastMessage(ChatColor.GOLD.toString() + "ランナーの死亡回数が上限に達したためゲームが終了したなぁ。")
            }
        }
    }

    @EventHandler
    fun onRespawn(event: PlayerRespawnEvent) {
        if (game.status != SpeedRunGame.Status.RUNNING) {
            return
        }
        val player: Player = event.getPlayer()
        if (player.getUniqueId() != game.runner) {
            player.getInventory().setItem(8, ItemStack(Material.COMPASS))
        }
    }

    @EventHandler
    fun onDeathEntity(event: EntityDeathEvent) {
        if (game.status != SpeedRunGame.Status.RUNNING) {
            return
        }
        if (event.getEntity().getType() == EntityType.ENDER_DRAGON) {
            game.stop()
            for (p in Bukkit.getServer().getOnlinePlayers()) {
                if (p.getUniqueId() != game.runner) p.playSound(
                    p.getLocation(),
                    Sound.ENTITY_LIGHTNING_BOLT_THUNDER,
                    1f,
                    1f
                )
                else p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
            }
            Bukkit.broadcastMessage(
                """
                    ${ChatColor.GOLD}エンダードラゴンが倒されたのでゲームが終了したなぁ。
                    ビクローイ！GG！
                    """.trimIndent()
            )
        }
    }
}
