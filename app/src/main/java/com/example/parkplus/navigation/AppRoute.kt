
package com.example.parkplus.navigation

sealed class AppRoute(val route: String) {

    data object Login : AppRoute("login")

    data object Parks : AppRoute("parks")

    data object Map : AppRoute("map")

    data object AddPark : AppRoute("add_park")

    data object Survey : AppRoute("survey/{parkId}") {
        fun createRoute(parkId: String) = "survey/$parkId"
    }

    data object SurveyDetails : AppRoute("survey_details/{surveyId}") {
        fun createRoute(surveyId: String) = "survey_details/$surveyId"
    }

    data object Camera : AppRoute("camera/{surveyId}") {
        fun createRoute(surveyId: String) = "camera/$surveyId"
    }
}
