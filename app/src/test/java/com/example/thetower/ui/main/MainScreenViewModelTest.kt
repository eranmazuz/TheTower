package com.example.thetower.ui.main

import com.example.thetower.data.DataRepository
import com.example.thetower.data.model.GameState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Assert.assertEquals
import org.junit.Test

class MainScreenViewModelTest {

    @Test
    fun testViewModelChooseJob() {
        val fakeRepository = FakeDataRepository()
        val viewModel = MainScreenViewModel(fakeRepository)

        // Default state has unselected job
        assertEquals("", viewModel.gameState.value.player.job)

        // Choose job
        viewModel.chooseJob("Lancelot", "Warrior")

        // Verify stats applied
        val player = viewModel.gameState.value.player
        assertEquals("Lancelot", player.name)
        assertEquals("Warrior", player.job)
        assertEquals(60, player.maxHp)
        assertEquals(12, player.baseAttack)
        assertEquals(6, player.baseDefense)
    }
}

private class FakeDataRepository : DataRepository {
    private val _gameState = MutableStateFlow(GameState())
    override val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    override fun updateState(mutation: (GameState) -> GameState): GameState {
        val newState = mutation(_gameState.value)
        _gameState.value = newState
        return newState
    }

    override fun loadState(): GameState = _gameState.value
}
