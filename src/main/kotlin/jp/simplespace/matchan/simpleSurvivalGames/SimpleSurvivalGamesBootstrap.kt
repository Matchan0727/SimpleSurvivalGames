package jp.simplespace.matchan.simpleSurvivalGames

import com.mojang.brigadier.Command
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.bootstrap.PluginProviderContext
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.plugin.java.JavaPlugin

class SimpleSurvivalGamesBootstrap : PluginBootstrap {

    override fun bootstrap(context: BootstrapContext) {
        val lifecycleManager = context.getLifecycleManager()
        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) {
            event ->
            val registrar = event.registrar()
            registrar.register(SimpleSurvivalCommands.game(), "ゲーム関連コマンド", listOf("game"))
            registrar.register(SimpleSurvivalCommands.speedrun(), "SpeedRun専用コマンド", listOf("sr"))
        }
    }

    override fun createPlugin(context: PluginProviderContext): JavaPlugin {
        return SimpleSurvivalGames()
    }
}