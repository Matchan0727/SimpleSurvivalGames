package jp.simplespace.matchan.simpleSurvivalGames

import jp.simplespace.matchan.simpleSurvivalGames.GameSelector.GameType.*
import jp.simplespace.matchan.simpleSurvivalGames.speedrun.SpeedRunGame
import jp.simplespace.simplehardcore.HardcoreGame

class GameSelector(val plugin: SimpleSurvivalGames) {
    companion object {
        var currentGameType = NONE
            private set
        var currentGame: IGame? = null
            private set
        lateinit var instance: GameSelector
    }

    init {
        init()
    }

    private fun init() {
        val config = plugin.config
        val gameType = config.getString("gametype")
        if (gameType != null) {
            val type = valueOf(gameType)
            if (type != NONE) {
                selectGame(type)
            }
        }
    }

    fun selectGame(type: GameType): SelectResult {
        if (currentGameType != NONE) return SelectResult.RUNNING_GAME
        if (type == currentGameType) {
            return SelectResult.ALREADY_SELECTED
        }
        val game: IGame = when (type) {
            SPEEDRUN -> SpeedRunGame(plugin)
            HARDCORE -> HardcoreGame(plugin)
            NONE -> return SelectResult.NONE_SELECTED
        }
        if (!game.init()) {
            if(game.canUnload()) {
                game.unload()
            }
            return SelectResult.FAILED
        }
        currentGameType = type
        currentGame = game
        saveConfig(type)
        return SelectResult.SUCCESS
    }

    fun canSelectGame(): Boolean {
        return currentGameType == NONE
    }

    fun unloadGame(): Boolean {
        if (currentGameType == NONE || !currentGame!!.canUnload()) return false
        val isSuccess: Boolean = when (currentGameType) {
            SPEEDRUN -> (currentGame as SpeedRunGame).unload()
            HARDCORE -> (currentGame as HardcoreGame).unload()
            NONE -> return false
        }
        if (isSuccess) {
            currentGame = null
            currentGameType = NONE
            saveConfig(NONE)
            return true
        }
        return false
    }

    private fun saveConfig(type: GameType) {
        val config = plugin.config
        config.set("gametype", type.name)
        plugin.saveConfig()
    }

    enum class GameType(val gameName: String) {
        SPEEDRUN("SpeedRun"),
        HARDCORE("ハードコア"),
        NONE("なし");

        override fun toString(): String {
            return gameName
        }
    }

    enum class SelectResult {
        SUCCESS,
        FAILED,
        ALREADY_SELECTED,
        NONE_SELECTED,
        RUNNING_GAME,
    }

}