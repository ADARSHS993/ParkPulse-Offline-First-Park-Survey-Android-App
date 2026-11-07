package com.example.parkplus.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.parkplus.presentation.addpark.AddParkScreen
import com.example.parkplus.presentation.auth.LoginScreen
import com.example.parkplus.presentation.parks.ParksScreen
import com.example.parkplus.presentation.map.MapScreen
import com.example.parkplus.presentation.survey.SurveyDetailsScreen
import com.example.parkplus.presentation.survey.SurveyScreen

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
            MapScreen(onAddPark = {
                navController.navigate(AppRoute.AddPark.route)
            })
        }

        composable(AppRoute.AddPark.route) {
            AddParkScreen(
                onParkSaved = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "survey/{parkId}",
            arguments = listOf(
                navArgument("parkId") {
                    type = NavType.StringType
                }
            )
        ) {
            SurveyScreen(
                onSurveyStarted = { surveyId ->
                    navController.navigate("survey_details/$surveyId")
                }
            )
        }

        composable("survey_details/{surveyId}") { backStackEntry ->

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


    }
}