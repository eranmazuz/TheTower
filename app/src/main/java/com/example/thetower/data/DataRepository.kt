package com.example.thetower.data

import android.content.Context
import com.example.thetower.data.model.GameState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import java.io.File

interface DataRepository {
    val gameState: StateFlow<GameState>
    fun updateState(mutation: (GameState) -> GameState): GameState
    fun loadState(): GameState
}

class DefaultDataRepository private constructor(private val context: Context) : DataRepository {

    private val fileLock = Any()
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val _gameState = MutableStateFlow(GameState())
    override val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    init {
        _gameState.value = loadStateSynchronously()
    }

    override fun updateState(mutation: (GameState) -> GameState): GameState {
        synchronized(fileLock) {
            val oldState = _gameState.value
            val newState = mutation(oldState).copy(lastSavedAt = System.currentTimeMillis())
            _gameState.value = newState
            saveStateSynchronously(newState)
            return newState
        }
    }

    override fun loadState(): GameState {
        return loadStateSynchronously()
    }

    private fun loadStateSynchronously(): GameState {
        synchronized(fileLock) {
            val targetFile = File(context.filesDir, "game_state.json")
            val backupFile = File(context.filesDir, "game_state.json.bak")
            
            val fileToRead = when {
                targetFile.exists() -> targetFile
                backupFile.exists() -> backupFile
                else -> null
            }
            
            if (fileToRead != null) {
                try {
                    val jsonString = fileToRead.readText()
                    return json.decodeFromString(GameState.serializer(), jsonString)
                } catch (e: Exception) {
                    e.printStackTrace()
                    if (fileToRead == targetFile && backupFile.exists()) {
                        try {
                            val backupString = backupFile.readText()
                            return json.decodeFromString(GameState.serializer(), backupString)
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }
                }
            }
            return GameState()
        }
    }

    private fun saveStateSynchronously(state: GameState) {
        synchronized(fileLock) {
            try {
                val jsonString = json.encodeToString(GameState.serializer(), state)
                val tempFile = File(context.filesDir, "game_state.json.tmp")
                val targetFile = File(context.filesDir, "game_state.json")
                val backupFile = File(context.filesDir, "game_state.json.bak")
                
                tempFile.writeText(jsonString)
                
                if (targetFile.exists()) {
                    if (backupFile.exists()) {
                        backupFile.delete()
                    }
                    targetFile.renameTo(backupFile)
                }
                
                if (!tempFile.renameTo(targetFile)) {
                    tempFile.copyTo(targetFile, overwrite = true)
                    tempFile.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        @Volatile
        private var instance: DefaultDataRepository? = null

        fun getInstance(context: Context): DefaultDataRepository {
            return instance ?: synchronized(this) {
                instance ?: DefaultDataRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
