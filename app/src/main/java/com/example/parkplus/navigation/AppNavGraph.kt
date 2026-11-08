
package com.example.parkplus.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.parkplus.presentation.addpark.AddParkScreen
import com.example.parkplus.presentation.auth.LoginScreen
import com.example.parkplus.presentation.camera.CameraRoute
import com.example.parkplus.presentation.map.MapScreen
import com.example.parkplus.presentation.parks.ParksScreen
import com.example.parkplus.presentation.survey.SurveyDetailsScreen
import com.example.parkplus.presentation.survey.SurveyScreen

@Composable
fun AppNavGraph() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppRoute.Login.route
    ) {

        // LOGIN
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

        // PARKS
        composable(AppRoute.Parks.route) {
            ParksScreen(
                onOpenMap = { parkId ->
                    navController.navigate(
                        "${AppRoute.Map.route}/$parkId"
                    )
                },
                onAddPark = {
                    navController.navigate(AppRoute.AddPark.route)
                }
            )
        }

        // MAP
        composable(
            route = "${AppRoute.Map.route}/{parkId}",
            arguments = listOf(
                navArgument("parkId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->

            val parkId = backStackEntry.arguments?.getString("parkId")
                ?: return@composable

            MapScreen(
                parkId = parkId,
                onAddPark = {
                    navController.navigate(AppRoute.AddPark.route)
                },
                onStartSurvey = { selectedParkId ->
                    navController.navigate(
                        AppRoute.Survey.createRoute(selectedParkId)
                    )
                }
            )
        }

        // ADD PARK
        composable(AppRoute.AddPark.route) {
            AddParkScreen(
                onParkSaved = {
                    navController.popBackStack()
                }
            )
        }

        // SURVEY CHECK-IN
        composable(
            route = AppRoute.Survey.route,
            arguments = listOf(
                navArgument("parkId") {
                    type = NavType.StringType
                }
            )
        ) {
            SurveyScreen(
                onSurveyStarted = { surveyId ->
                    navController.navigate(
                        AppRoute.SurveyDetails.createRoute(surveyId)
                    )
                }
            )
        }

        // SURVEY DETAILS
        composable(
            route = AppRoute.SurveyDetails.route,
            arguments = listOf(
                navArgument("surveyId") {
                    type = NavType.StringType
                }
            )
        ) {
            SurveyDetailsScreen(
                onSurveySaved = {
                    navController.navigate(AppRoute.Parks.route) {
                        popUpTo(AppRoute.Parks.route) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        // CAMERA
        composable(
            route = AppRoute.Camera.route,
            arguments = listOf(
                navArgument("surveyId") {
                    type = NavType.StringType
                }
            )
        ) {
            CameraRoute(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
