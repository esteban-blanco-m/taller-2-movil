package com.example.taller2_sophiemejia_estebanblanco.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.taller2_sophiemejia_estebanblanco.R
import com.example.taller2_sophiemejia_estebanblanco.navigation.AppScreens

@Composable
fun Home(controller: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally

        ) {
        Image(
            painter = painterResource(id = R.drawable.contacts),
            contentDescription = "contactos",
            modifier = Modifier
                .weight(1F)
                .fillMaxWidth()
                .padding(32.dp)
                .clickable {controller.navigate(AppScreens.Contacts.name) }

        )
        Image(
            painter = painterResource(id = R.drawable.camera),
            contentDescription = "camara",
            modifier = Modifier
                .weight(1F)
                .fillMaxWidth()
                .padding(32.dp)
                .clickable { controller.navigate(AppScreens.ImageGallery.name) }

        )
        Image(
            painter = painterResource(id = R.drawable.osmap),
            contentDescription = "mapa",
            modifier = Modifier
                .weight(1.2F)
                .fillMaxWidth()
                .padding(32.dp)
                .clickable { controller.navigate(AppScreens.Map.name) }


        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    val controller: NavController = rememberNavController()
    Home(controller)
}
