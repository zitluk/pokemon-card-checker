package com.pokemontcg.collection.ui.navigation

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pokemontcg.collection.R
import com.pokemontcg.collection.ui.AppViewModelFactory
import com.pokemontcg.collection.ui.collection.CollectionScreen
import com.pokemontcg.collection.ui.collection.CollectionViewModel
import com.pokemontcg.collection.ui.detail.CardDetailScreen
import com.pokemontcg.collection.ui.detail.CardDetailViewModel
import com.pokemontcg.collection.ui.edit.CollectionEditScreen
import com.pokemontcg.collection.ui.edit.CollectionEditViewModel
import com.pokemontcg.collection.ui.scan.ScanResultScreen
import com.pokemontcg.collection.ui.scan.ScanResultViewModel
import com.pokemontcg.collection.ui.scan.ScanScreen
import com.pokemontcg.collection.ui.scan.ScanViewModel
import com.pokemontcg.collection.ui.search.SearchViewModel
import com.pokemontcg.collection.ui.settings.SettingsScreen
import com.pokemontcg.collection.ui.settings.SettingsViewModel
import com.pokemontcg.collection.ui.theme.TcgChrome
import com.pokemontcg.collection.ui.theme.TcgColors

object Routes {
    const val COLLECTION = "collection"
    const val COLLECTION_SCAN = "collection/scan"
    const val SCAN_RESULT = "scan/result/{capturePath}"
    const val SETTINGS = "settings"
    const val CARD_DETAIL = "card/{cardId}"
    const val COLLECTION_EDIT = "collection/edit/{cardId}"
    const val COLLECTION_EDIT_ITEM = "collection/item/{itemId}"

    fun cardDetail(cardId: String) = "card/$cardId"
    fun collectionEdit(cardId: String) = "collection/edit/$cardId"
    fun collectionEditItem(itemId: Long) = "collection/item/$itemId"
    fun scanResult(capturePath: String) = "scan/result/${Uri.encode(capturePath)}"
}

@Composable
fun PokemonAppNav(factory: AppViewModelFactory) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBottomBar = currentRoute in listOf(
        Routes.COLLECTION,
        Routes.SETTINGS,
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = TcgColors.PlaymatSurface,
                    contentColor = TcgColors.OnPlaymatMuted,
                ) {
                    listOf(
                        Triple(Routes.COLLECTION, R.string.nav_collection, Icons.Default.Collections),
                        Triple(Routes.SETTINGS, R.string.nav_settings, Icons.Default.Settings),
                    ).forEach { (route, labelRes, icon) ->
                        val label = stringResource(labelRes)
                        NavigationBarItem(
                            selected = currentRoute == route,
                            colors = TcgChrome.navigationBarItemColors(),
                            onClick = {
                                navController.navigate(route) {
                                    popUpTo(Routes.COLLECTION) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.COLLECTION,
            modifier = Modifier.fillMaxSize(),
        ) {
            composable(Routes.COLLECTION) {
                val collectionVm: CollectionViewModel = viewModel(factory = factory)
                val searchVm: SearchViewModel = viewModel(factory = factory)
                CollectionScreen(
                    viewModel = collectionVm,
                    searchViewModel = searchVm,
                    contentPadding = padding,
                    onOpenItem = { navController.navigate(Routes.cardDetail(it.cardId)) },
                    onEditItem = { navController.navigate(Routes.collectionEditItem(it.id)) },
                    onStartScan = { navController.navigate(Routes.COLLECTION_SCAN) },
                    onSearchCardClick = { navController.navigate(Routes.cardDetail(it)) },
                )
            }
            composable(Routes.COLLECTION_SCAN) {
                val scanVm: ScanViewModel = viewModel(factory = factory)
                ScanScreen(
                    viewModel = scanVm,
                    contentPadding = padding,
                    onNavigateToResult = { path ->
                        navController.navigate(Routes.scanResult(path))
                    },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                Routes.SCAN_RESULT,
                arguments = listOf(navArgument("capturePath") { type = NavType.StringType }),
            ) { entry ->
                val encoded = entry.arguments?.getString("capturePath") ?: return@composable
                val capturePath = Uri.decode(encoded)
                val collectionEntry = navController.getBackStackEntry(Routes.COLLECTION)
                val collectionVm: CollectionViewModel = viewModel(collectionEntry)
                val resultVm: ScanResultViewModel = viewModel(factory = factory)
                val savedMessage = stringResource(R.string.scan_result_saved)
                ScanResultScreen(
                    capturePath = capturePath,
                    viewModel = resultVm,
                    contentPadding = padding,
                    onDiscard = {
                        navController.popBackStack(Routes.COLLECTION, inclusive = false)
                    },
                    onSaved = {
                        collectionVm.showUserMessage(savedMessage)
                        navController.popBackStack(Routes.COLLECTION, inclusive = false)
                    },
                )
            }
            composable(Routes.SETTINGS) {
                val vm: SettingsViewModel = viewModel(factory = factory)
                SettingsScreen(viewModel = vm, contentPadding = padding)
            }
            composable(
                Routes.CARD_DETAIL,
                arguments = listOf(navArgument("cardId") { type = NavType.StringType }),
            ) { entry ->
                val cardId = entry.arguments?.getString("cardId") ?: return@composable
                val vm: CardDetailViewModel = viewModel(factory = factory)
                CardDetailScreen(
                    cardId = cardId,
                    viewModel = vm,
                    contentPadding = padding,
                    onEditCollection = { navController.navigate(Routes.collectionEdit(cardId)) },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                Routes.COLLECTION_EDIT,
                arguments = listOf(navArgument("cardId") { type = NavType.StringType }),
            ) { entry ->
                val cardId = entry.arguments?.getString("cardId") ?: return@composable
                val vm: CollectionEditViewModel = viewModel(factory = factory)
                CollectionEditScreen(
                    cardId = cardId,
                    viewModel = vm,
                    contentPadding = padding,
                    onDone = { navController.popBackStack() },
                )
            }
            composable(
                Routes.COLLECTION_EDIT_ITEM,
                arguments = listOf(navArgument("itemId") { type = NavType.LongType }),
            ) { entry ->
                val itemId = entry.arguments?.getLong("itemId") ?: return@composable
                val vm: CollectionEditViewModel = viewModel(factory = factory)
                CollectionEditScreen(
                    itemId = itemId,
                    viewModel = vm,
                    contentPadding = padding,
                    onDone = { navController.popBackStack() },
                )
            }
        }
    }
}
