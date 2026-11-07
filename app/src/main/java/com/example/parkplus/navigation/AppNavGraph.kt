package com.example.parkplus.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.parkplus.presentation.auth.LoginScreen
import com.example.parkplus.presentation.parks.ParksScreen
import com.example.parkplus.presentation.map.MapScreen

@Composable
fun AppNavGraph() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppRoute.Login.route
    ) {

        composable(AppRoute.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(AppRoute.Parks.route) {
                        popUpTo(AppRoute.Login.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(AppRoute.Parks.route) {
            ParksScreen(
                onOpenMap = { parkId ->
                    navController.navigate(
                        "${AppRoute.Map.route}/$parkId"
                    )
                }
            )
        }

        composable(
            route = "${AppRoute.Map.route}/{parkId}",
            arguments = listOf(
                navArgument("parkId") {
                    type = NavType.StringType
                }
            )
        ) {
            MapScreen()
        }
    }
}