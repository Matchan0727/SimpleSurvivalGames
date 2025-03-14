package jp.simplespace.matchan.simpleSurvivalGames

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import jp.simplespace.matchan.simpleSurvivalGames.speedrun.SpeedRunGame
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class SimpleSurvivalCommands {
    companion object {
        fun game(): LiteralCommandNode<CommandSourceStack> {
            fun select(ctx: CommandContext<CommandSourceStack>, type: GameSelector.GameType): Int {
                val selector = GameSelector.instance
                if (!selector.canSelectGame()) {
                    ctx.source.sender.sendMessage(
                        Component.text("既にゲームが選択されています。").color(NamedTextColor.RED)
                    )
                    return Command.SINGLE_SUCCESS
                }
                when (selector.selectGame(type)) {
                    GameSelector.SelectResult.SUCCESS -> {
                        Bukkit.broadcastMessage(ChatColor.GOLD.toString() + "ゲームモードを" + type.toString() + "に設定しました。")
                    }
                    GameSelector.SelectResult.RUNNING_GAME -> {
                        ctx.source.sender.sendMessage(
                            Component.text("既にゲームが実行中です。").color(NamedTextColor.RED)
                        )
                    }
                    GameSelector.SelectResult.ALREADY_SELECTED -> {
                        ctx.source.sender.sendMessage(
                            Component.text("既に" + type + "ゲームが選択されています。").color(NamedTextColor.RED)
                        )
                    }
                    GameSelector.SelectResult.FAILED -> {
                        ctx.source.sender.sendMessage(
                            Component.text("ゲームの初期化に失敗しました。").color(NamedTextColor.RED)
                        )
                    }
                    GameSelector.SelectResult.NONE_SELECTED -> {
                        ctx.source.sender.sendMessage(
                            Component.text("ゲームが選択されていません。").color(NamedTextColor.RED)
                        )
                    }
                }
                return Command.SINGLE_SUCCESS
            }
            return Commands.literal("srvgame")
                .requires{ source -> source.sender.hasPermission("ssg.command.game") }
                .then(Commands.literal("unload")
                    .executes { ctx ->
                        if (GameSelector.instance.unloadGame()) {
                            Bukkit.broadcastMessage(ChatColor.GOLD.toString() + "ゲームをアンロードしました。")
                        } else {
                            ctx.source.sender.sendMessage(
                                Component.text("ゲームのアンロードに失敗しました。").color(NamedTextColor.RED)
                            )
                        }
                        Command.SINGLE_SUCCESS
                    }
                )
                .then(Commands.literal("speedrun")
                    .executes { ctx -> select(ctx, GameSelector.GameType.SPEEDRUN) })
                .then(Commands.literal("hardcore")
                    .executes { ctx -> select(ctx, GameSelector.GameType.HARDCORE) }
                )
                .build()
        }

        fun speedrun(): LiteralCommandNode<CommandSourceStack> {
            fun check(ctx: CommandContext<CommandSourceStack>): Boolean {
                val boo = GameSelector.currentGameType == GameSelector.GameType.SPEEDRUN
                if (!boo) {
                    ctx.source.sender.sendMessage(
                        Component.text("SpeedRunゲームが選択されていません。").color(NamedTextColor.RED)
                    )
                }
                return boo
            }

            fun game(): SpeedRunGame {
                return GameSelector.currentGame as SpeedRunGame
            }

            return Commands.literal("speedrun")
                .requires { source -> source.sender.hasPermission("ssg.command.speedrun") }
                .then(
                    Commands.literal("start")
                        .executes { ctx ->
                            if (!check(ctx)) return@executes Command.SINGLE_SUCCESS
                            val game = game()
                            val sender = ctx.source.sender
                            if (game.runner == null) {
                                sender.sendMessage(ChatColor.RED.toString() + "ランナーが設定されていません。")
                                return@executes Command.SINGLE_SUCCESS
                            }
                            if (game.status == SpeedRunGame.Status.READY) {
                                game.start()
                                Bukkit.broadcastMessage(ChatColor.GOLD.toString() + "ゲームを強制スタートさせました。")
                                return@executes Command.SINGLE_SUCCESS
                            }
                            if (!game.ready()) {
                                sender.sendMessage(ChatColor.RED.toString() + "ゲームを開始できませんでした。")
                            }
                            Command.SINGLE_SUCCESS
                        }
                )
                .then(
                    Commands.literal("stop")
                        .executes { ctx ->
                            if (!check(ctx)) return@executes Command.SINGLE_SUCCESS
                            val game = game()
                            val sender = ctx.source.sender
                            if (!game.stop()) {
                                sender.sendMessage(ChatColor.RED.toString() + "現在ゲームが実行されていません。")
                                return@executes Command.SINGLE_SUCCESS
                            }
                            for (player in Bukkit.getOnlinePlayers()) {
                                player.inventory.clear()
                            }
                            Command.SINGLE_SUCCESS
                        }
                )
                .then(
                    Commands.literal("setinterval")
                    .then(
                        Commands.argument("second", IntegerArgumentType.integer())
                            .executes { ctx ->
                                if (!check(ctx)) return@executes Command.SINGLE_SUCCESS
                                val game = game()
                                val config = game.config
                                val sec = IntegerArgumentType.getInteger(ctx, "second")
                                config["compassInterval"] = sec
                                game.saveConfig()
                                game.scheduler.compassCount = sec
                                Bukkit.broadcastMessage(ChatColor.GOLD.toString() + "コンパスの更新間隔を" + sec + "秒に設定しました。")
                                Command.SINGLE_SUCCESS
                            }
                    )
                )
                .then(
                    Commands.literal("setrunner")
                    .then(
                        Commands.argument("target", ArgumentTypes.player())
                            .executes { ctx ->
                                if (!check(ctx)) return@executes Command.SINGLE_SUCCESS
                                val game = game()
                                val resolver = ctx.getArgument("target", PlayerSelectorArgumentResolver::class.java)
                                val target = resolver.resolve(ctx.source).first()
                                game.setRunner(target)
                                Bukkit.broadcastMessage(ChatColor.GOLD.toString() + "ランナーを" + target.getName() + "に設定しました。")
                                Command.SINGLE_SUCCESS
                            }
                    )
                )
                .then(
                    Commands.literal("setcompassannounce")
                        .then(Commands.argument("bool", BoolArgumentType.bool()))
                        .executes { ctx ->
                            if (!check(ctx)) return@executes Command.SINGLE_SUCCESS
                            val game = game()
                            val config = game.config
                            val boo = BoolArgumentType.getBool(ctx, "bool")
                            config["compassAnnounce"] = boo
                            game.saveConfig()
                            Bukkit.broadcastMessage(ChatColor.GOLD.toString() + "コンパス更新の通知を" + boo + "に設定しました。")
                            Command.SINGLE_SUCCESS
                        })
                .then(
                    Commands.literal("setmaxdeath")
                        .then(Commands.argument("count", IntegerArgumentType.integer()))
                        .executes { ctx ->
                            if (!check(ctx)) return@executes Command.SINGLE_SUCCESS
                            val game = game()
                            val config = game.config
                            val count = IntegerArgumentType.getInteger(ctx, "count")
                            config["maxDeath"] = count
                            game.saveConfig()
                            Bukkit.broadcastMessage(ChatColor.GOLD.toString() + "ランナーの最大死亡可能回数を" + count + "回に設定しました。")
                            Command.SINGLE_SUCCESS
                        }
                )
                .then(
                    Commands.literal("pause")
                        .executes { ctx ->
                            if (!check(ctx)) return@executes Command.SINGLE_SUCCESS
                            val game = game()
                            if (game.isPause) {
                                game.isPause(false)
                                Bukkit.broadcastMessage(ChatColor.GOLD.toString() + "ゲームを再開させました。")
                            } else {
                                game.isPause(true)
                                Bukkit.broadcastMessage(ChatColor.GOLD.toString() + "ゲームを一時停止させました。")
                            }
                            Command.SINGLE_SUCCESS
                        }
                )
                .then(
                    Commands.literal("pvp")
                        .executes { ctx ->
                            if (!check(ctx)) return@executes Command.SINGLE_SUCCESS
                            val game = game()
                            val sender = ctx.source.sender
                            if (game.status == SpeedRunGame.Status.READY) {
                                for (player in Bukkit.getOnlinePlayers()) {
                                    val inv = player.inventory
                                    inv.clear()
                                    inv.helmet = ItemStack(Material.IRON_HELMET)
                                    inv.chestplate = ItemStack(Material.IRON_CHESTPLATE)
                                    inv.leggings = ItemStack(Material.IRON_LEGGINGS)
                                    inv.boots = ItemStack(Material.IRON_BOOTS)
                                    inv.setItem(8, ItemStack(Material.COMPASS))
                                    inv.setItem(0, ItemStack(Material.IRON_SWORD))
                                    inv.setItem(1, ItemStack(Material.COOKED_BEEF, 64))
                                    inv.setItem(2, ItemStack(Material.GOLDEN_APPLE, 16))
                                    inv.heldItemSlot = 0
                                }
                                Bukkit.broadcastMessage(ChatColor.GOLD.toString() + "全プレイヤーにPvP装備一式を付与しました。")
                            } else {
                                sender.sendMessage(ChatColor.RED.toString() + "ゲームが準備状態のときのみ使用できます。")
                            }
                            Command.SINGLE_SUCCESS
                        })
                .build()
        }
    }
}