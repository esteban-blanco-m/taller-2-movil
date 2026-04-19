package com.example.taller2_sophiemejia_estebanblanco.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.taller2_sophiemejia_estebanblanco.screens.Contacts
import com.example.taller2_sophiemejia_estebanblanco.screens.Home
import com.example.taller2_sophiemejia_estebanblanco.screens.ImageGallery
import com.example.taller2_sophiemejia_estebanblanco.screens.MapScreen

enum class AppScreens {
    Home, Contacts, ImageGallery, Map

}


@Composable
fun Navigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = AppScreens.Home.name) {
        composable(route = AppScreens.Home.name) {
            Home(navController)
        }
        composable(route = AppScreens.Contacts.name) {
            Contacts()
        }
        composable(route = AppScreens.ImageGallery.name) {
            ImageGallery()
        }
        composable(route = AppScreens.Map.name) {
            MapScreen()
        }

    }
}
