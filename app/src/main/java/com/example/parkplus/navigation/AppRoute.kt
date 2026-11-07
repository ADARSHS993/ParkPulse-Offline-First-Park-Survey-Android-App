package com.example.parkplus.navigation

sealed class AppRoute(val route: String){

    data object Login : AppRoute("login")

    data object Parks : AppRoute("parks")

    data object Map : AppRoute("map")

}