package com.example.mycrudclientes

data class Cliente(
    var id: Long,
    var nombre: String,
    var direccion: String,
    var telefono: String,
    var correo: String,
    var fotoUrl: String? = null
)
