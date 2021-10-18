package com.example.poi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import android.security.identity.InvalidRequestMessageException
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.example.poi.adaptadores.ChatAdaptador
import com.example.poi.modelos.Mensaje
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ActivityChat : AppCompatActivity() {

    private val messageList = mutableListOf<Mensaje>()
    private lateinit var chatAdapter: ChatAdaptador
    private lateinit var rvChats: RecyclerView
    private lateinit var username: String
    private lateinit var usernameChat: String
    private lateinit var chatKey: String
    private val database = FirebaseDatabase.getInstance()
    private val authen = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        rvChats = findViewById(R.id.rvMsgRecibidos)

        chatAdapter = ChatAdaptador(messageList)
        rvChats.adapter = chatAdapter

        username = intent.getStringExtra("username") ?: "Sin nombre"
        usernameChat = intent.getStringExtra("usernameChat") ?: "Sin nombre"
        chatKey = intent.getStringExtra("keyChat") ?: "Sin nombre"

        Log.d("Main", "$username")
        Log.d("Main", "$usernameChat")
        Log.d("Main", "$chatKey")

        findViewById<Button>(R.id.btn_Enviar).setOnClickListener {
            val message = findViewById<EditText>(R.id.edit_EnviarMsg).text.toString()
            if (message.isNotEmpty()) {
                val fromId = authen.uid ?: ""
                val toId = chatKey
                val msg = Mensaje("", message, username, ServerValue.TIMESTAMP, fromId, toId)
                sendMessage(msg)
            }
            val editMsg = findViewById<EditText>(R.id.edit_EnviarMsg)
            editMsg.setText("")
        }
        readMessage()
    }

    private fun sendMessage(message: Mensaje) {
        val reference = database.getReference("/chat-user-to-user/${message.fromid}/${message.toid}").push()
        val toReference = database.getReference("/chat-user-to-user/${message.toid}/${message.fromid}").push()

        message.id = reference.key ?: ""

        reference.setValue(message)
        toReference.setValue(message)
    }

    private fun readMessage() {
        val fromId = authen.uid ?: ""
        val toId = chatKey
        database.getReference("/chat-user-to-user/$fromId/$toId").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messageList.clear()

                for(snap in snapshot.children) {
                    val currentMessage: Mensaje = snap.getValue(Mensaje::class.java) as Mensaje
                    if(currentMessage.fromid == authen.uid || currentMessage.toid == authen.uid) {
                        messageList.add(currentMessage)
                    }
                }

                if(messageList.size > 0) {
                    chatAdapter.notifyDataSetChanged()
                    rvChats.smoothScrollToPosition(messageList.size - 1)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}