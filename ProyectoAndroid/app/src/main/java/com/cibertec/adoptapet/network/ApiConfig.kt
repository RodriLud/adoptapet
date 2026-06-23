package com.cibertec.adoptapet.network

object ApiConfig {
    // Emulador Android: 10.0.2.2 apunta a la PC donde corre Spring Boot.
    //const val BASE_URL = "http://10.0.2.2:8080/"
    // Celular fisico por USB: ejecutar adb reverse tcp:8080 tcp:8080.
    const val BASE_URL = "http://192.168.100.14:8080/"
}
