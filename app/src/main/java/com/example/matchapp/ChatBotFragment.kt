package com.example.matchapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class ChatBotFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var closeButton: ImageButton
    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()

    // Cliente HTTP con timeouts extendidos
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val SERVER_URL = "https://eight-grapes-laugh.loca.lt/api/v1/whatsapp/answers"

    companion object {
        private const val TAG = "ChatBotFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_chat_bot, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView   = view.findViewById(R.id.recyclerMessages)
        messageInput   = view.findViewById(R.id.editTextMessage)
        sendButton     = view.findViewById(R.id.btnSend)
        closeButton    = view.findViewById(R.id.btnClose)

        chatAdapter = ChatAdapter(messages)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter       = chatAdapter
        }

        // Bienvenida
        addBotMessage("¡Hola! Envíame tu pregunta sobre UP…")
        //addBotMessage("🌐 Conectando a: $SERVER_URL")

        sendButton.setOnClickListener {
            val text = messageInput.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener
            addUserMessage(text)
            messageInput.text.clear()
            sendToServer(text)
        }

        closeButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun sendToServer(userMsg: String) {
        Log.d(TAG, "POST a $SERVER_URL → $userMsg")

        // Construimos JSON
        val json = JSONObject().apply {
            put("from_phone", "3310222500")
            put("message", userMsg)
        }.toString()

        // Creamos el body y la request
        val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(SERVER_URL)
            .post(body)
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            .build()

        // UI: bloqueo y loading
        sendButton.isEnabled = false
        addBotMessage("⏳ Enviando…")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Error de conexión", e)
                activity?.runOnUiThread {
                    removeLoadingMessage()
                    addBotMessage("❌ Conexión fallida: ${e.localizedMessage}")
                    sendButton.isEnabled = true
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val code = response.code
                val bodyStr = response.body?.string().orEmpty()
                Log.d(TAG, "HTTP $code → $bodyStr")
                activity?.runOnUiThread {
                    removeLoadingMessage()
                    if (response.isSuccessful) {
                        try {
                            // El servidor puede devolver { "data":[ "... ] } o { "reply":"..." }
                            val jsonResp = JSONObject(bodyStr)
                            val reply = when {
                                jsonResp.has("data") ->
                                    jsonResp.getJSONArray("data").optString(0, "–")
                                jsonResp.has("reply") ->
                                    jsonResp.optString("reply", "–")
                                else -> bodyStr
                            }
                            addBotMessage(reply)
                        } catch (ex: Exception) {
                            Log.e(TAG, "Parseo falló", ex)
                            addBotMessage("⚠️ Error procesando respuesta")
                        }
                    } else {
                        addBotMessage("⚠️ HTTP $code")
                    }
                    sendButton.isEnabled = true
                }
                response.close()
            }
        })
    }

    private fun removeLoadingMessage() {
        if (messages.isNotEmpty() && messages.last().text.startsWith("⏳")) {
            messages.removeAt(messages.lastIndex)
            chatAdapter.notifyItemRemoved(messages.size)
        }
    }

    private fun addUserMessage(msg: String) {
        messages.add(ChatMessage(msg, true))
        chatAdapter.notifyItemInserted(messages.lastIndex)
        recyclerView.scrollToPosition(messages.lastIndex)
    }

    private fun addBotMessage(msg: String) {
        messages.add(ChatMessage(msg, false))
        chatAdapter.notifyItemInserted(messages.lastIndex)
        recyclerView.scrollToPosition(messages.lastIndex)
    }
}

data class ChatMessage(val text: String, val isUser: Boolean)

class ChatAdapter(private val items: List<ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val text: TextView = view.findViewById(R.id.textMessage)
    }

    override fun getItemViewType(position: Int) =
        if (items[position].isUser) R.layout.item_message_user else R.layout.item_message_bot

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(viewType, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.text.text = items[position].text
    }

    override fun getItemCount() = items.size
}