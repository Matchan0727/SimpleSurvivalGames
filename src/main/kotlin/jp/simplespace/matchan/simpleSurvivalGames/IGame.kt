package jp.simplespace.matchan.simpleSurvivalGames

interface IGame {
    fun unload(): Boolean
    fun canUnload(): Boolean
    fun init(): Boolean
}