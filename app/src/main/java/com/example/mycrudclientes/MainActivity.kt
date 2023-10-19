package com.example.mycrudclientes

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var listView: ListView
    private lateinit var dbHandler: ClientsDbHandler
    private lateinit var clients: List<Cliente>
    private val CLIENT_DETAIL_REQUEST_CODE = 1
    private lateinit var emptyListMessage: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listView = findViewById(R.id.listView)
        dbHandler = ClientsDbHandler(this)
        emptyListMessage = findViewById(R.id.emptyListMessage)

        // Inicialmente, oculta el mensaje de lista vacía
        emptyListMessage.visibility = View.GONE

        updateListView()

        listView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val intent = Intent(this, ClienteDetailActivity::class.java)
                intent.putExtra("CLIENT_ID", clients[position].id)
                startActivityForResult(intent, CLIENT_DETAIL_REQUEST_CODE)
            }
    }

    fun addClient(view: android.view.View) {
        val intent = Intent(this, ClienteDetailActivity::class.java)
        startActivityForResult(intent, CLIENT_DETAIL_REQUEST_CODE)
    }

    // Sobrescribe el método onActivityResult para manejar los resultados de ClienteDetailActivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CLIENT_DETAIL_REQUEST_CODE && resultCode == RESULT_OK) {
            updateListView()
        }
    }

    private fun updateListView() {
        clients = dbHandler.getAllClients()
        val clientNames = ArrayList<String>()
        for (cliente in clients) {
            clientNames.add(cliente.nombre)
        }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, clientNames)
        listView.adapter = adapter

        // Muestra u oculta el mensaje de lista vacía según el estado de la lista
        emptyListMessage.visibility = if (clients.isEmpty()) View.VISIBLE else View.GONE
    }
}