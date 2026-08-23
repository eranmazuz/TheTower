package com.example.thetower.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.example.thetower.R
import com.example.thetower.data.DefaultDataRepository
import com.example.thetower.ui.CodexScreen
import com.example.thetower.ui.HeroScreen
import com.example.thetower.ui.JobSelectionScreen
import com.example.thetower.ui.LanguageWrapper
import com.example.thetower.ui.SettingsScreen
import com.example.thetower.ui.TowerScreen
import com.example.thetower.ui.TownScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onItemClick: (NavKey) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mainViewModel: MainScreenViewModel = viewModel {
        MainScreenViewModel(DefaultDataRepository.getInstance(context))
    }

    val state by mainViewModel.gameState.collectAsStateWithLifecycle()

    // 1. Run slot evaluation and Alarm setup on startup/resume
    LaunchedEffect(Unit) {
        mainViewModel.resumeAndEvaluate(context)
    }

    // 2. Handle Job selection if not choosing yet
    if (state.player.job.isEmpty()) {
        JobSelectionScreen(
            onJobSelected = { name, job ->
                mainViewModel.chooseJob(name, job)
            }
        )
        return
    }

    // 3. Main layout wrapped with dynamic RTL LanguageWrapper
    LanguageWrapper(language = state.appLanguage) {
        var selectedTab by remember { mutableIntStateOf(0) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = when (selectedTab) {
                                0 -> stringResource(R.string.tab_tower)
                                1 -> stringResource(R.string.tab_town)
                                2 -> stringResource(R.string.tab_hero)
                                3 -> stringResource(R.string.tab_codex)
                                4 -> stringResource(R.string.tab_settings)
                                else -> stringResource(R.string.app_name)
                            }
                        )
                    }
                )
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Text("🛕", fontSize = 20.sp) },
                        label = { Text(stringResource(R.string.tab_tower)) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Text("🏘️", fontSize = 20.sp) },
                        label = { Text(stringResource(R.string.tab_town)) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Text("👤", fontSize = 20.sp) },
                        label = { Text(stringResource(R.string.tab_hero)) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = { Text("📖", fontSize = 20.sp) },
                        label = { Text(stringResource(R.string.tab_codex)) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        icon = { Text("⚙️", fontSize = 20.sp) },
                        label = { Text(stringResource(R.string.tab_settings)) }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedTab) {
                    0 -> TowerScreen(
                        state = state,
                        onUsePotion = { mainViewModel.useHealthPotion() },
                        onEscape = { mainViewModel.escapeTown() },
                        onFightBoss = { mainViewModel.fightFloorBoss() }
                    )
                    1 -> TownScreen(
                        state = state,
                        onBuyItem = { itemId -> mainViewModel.buyItem(itemId) },
                        onSellItem = { instanceId -> mainViewModel.sellItem(instanceId) },
                        onAddQuest = { title, slot, days -> mainViewModel.addQuest(title, slot, days) },
                        onDeleteQuest = { questId -> mainViewModel.deleteQuest(questId) }
                    )
                    2 -> HeroScreen(
                        state = state,
                        onAddWater = { amount -> mainViewModel.addWater(amount) },
                        onLogTemptation = { mainViewModel.logTemptation() },
                        onEquip = { instanceId -> mainViewModel.equipItem(instanceId) },
                        onUnequip = { instanceId -> mainViewModel.unequipItem(instanceId) },
                        onUsePotion = { mainViewModel.useHealthPotion() }
                    )
                    3 -> CodexScreen(state = state)
                    4 -> SettingsScreen(
                        state = state,
                        onLanguageChange = { lang -> mainViewModel.updateLanguage(lang) },
                        onAlarmTimesChange = { times ->
                            mainViewModel.updateAlarmTimes(times)
                            mainViewModel.resumeAndEvaluate(context)
                        },
                        onAlarmModeToggle = { active -> mainViewModel.toggleAlarmMode(active) },
                        onHydrationTargetChange = { target -> mainViewModel.setHydrationTarget(target) },
                        onRingtoneUploaded = { mainViewModel.saveCustomRingtonePath() }
                    )
                }
            }
        }
    }
}
