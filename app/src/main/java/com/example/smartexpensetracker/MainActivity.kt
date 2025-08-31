package com.example.smartexpensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.smartexpensetracker.ui.screens.ExpenseEntryScreen
import com.example.smartexpensetracker.ui.screens.ExpenseListScreen
import com.example.smartexpensetracker.ui.screens.ExpenseReportScreen
import com.example.smartexpensetracker.ui.theme.SmartExpenseTheme
import com.example.smartexpensetracker.ui.theme.ThemeManager
import com.example.smartexpensetracker.ui.vm.ExpenseViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeManager = remember { ThemeManager(applicationContext) }

            // collect current theme preference, default to system if null
            val systemDark = isSystemInDarkTheme()
            val isDark by themeManager.isDark.collectAsState(initial = systemDark)

            val navController = rememberNavController()
            val vm: ExpenseViewModel = viewModel(
                factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
            )

            val items = listOf(Screen.List, Screen.Entry, Screen.Report)

            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()

            SmartExpenseTheme(darkTheme = isDark) {
                Scaffold(
                    modifier = Modifier.systemBarsPadding(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    "Smart Daily Expense Tracker",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                                actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            actions = {
                                IconButton(onClick = {
                                    scope.launch { themeManager.setDark(!isDark) }
                                }) {
                                    Icon(
                                        imageVector = if (isDark) Icons.Default.Brightness7 else Icons.Default.Brightness4,
                                        contentDescription = "Toggle theme"
                                    )
                                }
                            }
                        )
                    },
                    bottomBar = {
                        NavigationBar {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentRoute = navBackStackEntry?.destination?.route
                            items.forEach { screen ->
                                NavigationBarItem(
                                    icon = { Icon(screen.icon, contentDescription = screen.label) },
                                    label = { Text(screen.label) },
                                    selected = currentRoute == screen.route,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        indicatorColor = MaterialTheme.colorScheme.primary.copy(
                                            alpha = 0.2f
                                        )
                                    )
                                )
                            }
                        }
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { padding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.List.route,
                        modifier = Modifier.padding(padding)
                    ) {
                        composable(Screen.List.route) {
                            ExpenseListScreen(
                                vm = vm,
                                navController = navController
                            )
                        }
                        composable(Screen.Entry.route) {
                            ExpenseEntryScreen(vm = vm, navController = navController)
                        }
                        composable(Screen.Report.route) {
                            ExpenseReportScreen(vm = vm)
                        }
                    }
                }
            }
        }
    }
}

sealed class Screen(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    object List : Screen("list", "List", Icons.AutoMirrored.Filled.ReceiptLong)
    object Entry : Screen("entry", "Entry", Icons.Default.CurrencyRupee)
    object Report : Screen("report", "Report", Icons.Default.BarChart)
}
