package com.example.taller2_sophiemejia_estebanblanco.screens

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.fillMaxWidth
import android.Manifest
import android.content.ContentResolver
import android.provider.ContactsContract
import androidx.compose.runtime.Composable
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taller2_sophiemejia_estebanblanco.R
import com.google.accompanist.permissions.rememberPermissionState

data class Contacto(val id: String, val nombre: String, val telefono: String)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Contacts() {
    val contexto = LocalContext.current
    val contentResolver: ContentResolver = contexto.contentResolver
    val estadoPermisoContactos = rememberPermissionState(Manifest.permission.READ_CONTACTS)


    if (estadoPermisoContactos.status.isGranted) {
        val contactos = cargarContactos(contentResolver)
        LazyColumn {
            items(contactos) { contacto ->
                Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = contacto.nombre, fontSize = 20.sp)
                        Text(text = contacto.telefono, fontSize = 16.sp, color = colorResource(R.color.gray))
                    }
                }
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Se requiere permiso para leer contactos")
            Button(onClick = { estadoPermisoContactos.launchPermissionRequest() }) {
                Text("Solicitar Permiso")
            }
        }
    }
}





fun cargarContactos(contentResolver: ContentResolver): List<Contacto> {
    val contactos = mutableListOf<Contacto>()
    val projection = arrayOf(
        ContactsContract.CommonDataKinds.Phone._ID,
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
        ContactsContract.CommonDataKinds.Phone.NUMBER
    )
    val cursor = contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        projection, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
    )

    if (cursor != null) {
        val idColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID)
        val nameColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
        val numberColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

        while (cursor.moveToNext()) {
            val id = cursor.getString(idColumn)
            val nombre = cursor.getString(nameColumn)
            val numero = cursor.getString(numberColumn)
            contactos.add(Contacto(id, nombre, numero))
        }
        cursor.close()
    }
    return contactos
}

