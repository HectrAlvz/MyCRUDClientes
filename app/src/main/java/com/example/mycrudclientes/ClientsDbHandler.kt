package com.example.mycrudclientes

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ClientsDbHandler(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "ClientesDatabase"

        private const val TABLE_CLIENTS = "Clientes"

        private const val KEY_ID = "id"
        private const val KEY_NOMBRE = "nombre"
        private const val KEY_DIRECCION = "direccion"
        private const val KEY_TELEFONO = "telefono"
        private const val KEY_CORREO = "correo"
        private const val KEY_FOTO_URL = "fotoUrl"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = ("CREATE TABLE $TABLE_CLIENTS(" +
                "$KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$KEY_NOMBRE TEXT," +
                "$KEY_DIRECCION TEXT," +
                "$KEY_TELEFONO TEXT," +
                "$KEY_CORREO TEXT," +
                "$KEY_FOTO_URL TEXT)")
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_CLIENTS")
        onCreate(db)
    }

    fun addClient(cliente: Cliente): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_NOMBRE, cliente.nombre)
        values.put(KEY_DIRECCION, cliente.direccion)
        values.put(KEY_TELEFONO, cliente.telefono)
        values.put(KEY_CORREO, cliente.correo)
        values.put(KEY_FOTO_URL, cliente.fotoUrl)
        return db.insert(TABLE_CLIENTS, null, values)
    }

    fun getClient(id: Long): Cliente? {
        val db = this.readableDatabase
        val cursor: Cursor = db.query(
            TABLE_CLIENTS, arrayOf(
                KEY_ID,
                KEY_NOMBRE,
                KEY_DIRECCION,
                KEY_TELEFONO,
                KEY_CORREO,
                KEY_FOTO_URL
            ),
            "$KEY_ID=?",
            arrayOf(id.toString()), null, null, null, null
        )
        return if (cursor != null) {
            cursor.moveToFirst()
            val cliente = Cliente(
                cursor.getLong(0),
                cursor.getString(1),
                cursor.getString(2),
                cursor.getString(3),
                cursor.getString(4),
                cursor.getString(5)
            )
            cursor.close()
            cliente
        } else {
            null
        }
    }

    // Eliminar un cliente
    fun deleteClient(cliente: Cliente) {
        val db = this.writableDatabase
        db.delete(
            TABLE_CLIENTS,
            "$KEY_ID = ?",
            arrayOf(cliente.id.toString())
        )
        db.close()
    }

    // Actualizar un cliente existente
    fun updateClient(cliente: Cliente): Int {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_NOMBRE, cliente.nombre)
        values.put(KEY_DIRECCION, cliente.direccion)
        values.put(KEY_TELEFONO, cliente.telefono)
        values.put(KEY_CORREO, cliente.correo)
        values.put(KEY_FOTO_URL, cliente.fotoUrl)

        // Actualizar la fila en la tabla
        return db.update(
            TABLE_CLIENTS,
            values,
            "$KEY_ID = ?",
            arrayOf(cliente.id.toString())
        )
    }

    // Obtener la lista de todos los clientes
    fun getAllClients(): List<Cliente> {
        val clientList = ArrayList<Cliente>()
        val selectQuery = "SELECT * FROM $TABLE_CLIENTS"
        val db = this.writableDatabase
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {
            do {
                val cliente = Cliente(
                    cursor.getLong(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5)
                )
                clientList.add(cliente)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return clientList
    }
}