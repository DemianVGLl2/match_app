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
import org.json.JSONArray
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

    // Cliente HTTP con timeouts más largos
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    // URLs CORREGIDAS - Cambia según tu entorno
    private val SERVER_URLS = arrayOf(
        // Para EMULADOR ANDROID (10.0.2.2 mapea a localhost de tu PC)
        "http://10.0.2.2:8069/api/v1/whatsapp/answers",

        // Para DISPOSITIVO FÍSICO (usa tu IP de red)
        "http://192.168.100.18:8069/api/v1/whatsapp/answers",

        // Alternativa: encuentra tu IP con 'ipconfig' en Windows
        "http://192.168.100.12:8069/api/v1/whatsapp/answers"
    )

    private var currentUrlIndex = 0
    private val SERVER_URL get() = SERVER_URLS[currentUrlIndex]

    companion object {
        private const val TAG = "ChatBotFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat_bot, container, false)
    }

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

        // Mensaje de bienvenida
        addBotMessage("¡Hola! ¿En qué puedo ayudarte hoy?")
        addBotMessage("🔧 Probando servidor: $SERVER_URL")

        // Mostrar información del dispositivo
        val deviceInfo = if (isEmulator()) "📱 Emulador detectado" else "📱 Dispositivo físico detectado"
        addBotMessage(deviceInfo)

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

        // Prueba de conectividad automática
        testConnection()
    }

    private fun isEmulator(): Boolean {
        return (android.os.Build.FINGERPRINT.startsWith("generic") ||
                android.os.Build.FINGERPRINT.startsWith("unknown") ||
                android.os.Build.MODEL.contains("google_sdk") ||
                android.os.Build.MODEL.contains("Emulator") ||
                android.os.Build.MODEL.contains("Android SDK built for x86") ||
                android.os.Build.MANUFACTURER.contains("Genymotion") ||
                android.os.Build.BRAND.startsWith("generic") && android.os.Build.DEVICE.startsWith("generic"))
    }

    private fun testConnection() {
        addBotMessage("🔍 Probando conectividad...")

        // Crear request simple para probar
        val testPayload = JSONObject().apply {
            put("from_phone", "test")
            put("message", "test connection")
        }.toString()

        val body = testPayload.toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(SERVER_URL)
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Prueba de conexión falló con $SERVER_URL", e)
                activity?.runOnUiThread {
                    if (currentUrlIndex < SERVER_URLS.size - 1) {
                        currentUrlIndex++
                        addBotMessage("❌ Falló: ${SERVER_URLS[currentUrlIndex-1]}")
                        testConnection()
                    } else {
                        addBotMessage("⚠️ Ninguna URL funcionó")
                        showConnectionHelp()
                        currentUrlIndex = 0
                    }
                }
            }

            override fun onResponse(call: Call, response: Response) {
                activity?.runOnUiThread {
                    if (response.isSuccessful) {
                        addBotMessage("✅ Conexión exitosa con: $SERVER_URL")
                        addBotMessage("🚀 ¡Listo para chatear!")
                    } else {
                        addBotMessage("⚠️ Servidor responde pero con error: ${response.code}")
                        if (currentUrlIndex < SERVER_URLS.size - 1) {
                            currentUrlIndex++
                            testConnection()
                        }
                    }
                }
                response.close()
            }
        })
    }

    private fun showConnectionHelp() {
        addBotMessage("🛠️ AYUDA PARA CONECTAR:")
        addBotMessage("1. Verifica que Docker esté corriendo")
        addBotMessage("2. Confirma que el puerto 8069 esté expuesto")
        addBotMessage("3. Ejecuta: docker ps")
        addBotMessage("4. Busca: 0.0.0.0:8069->8069/tcp")

        if (isEmulator()) {
            addBotMessage("📱 Para emulador, usa: http://10.0.2.2:8069")
        } else {
            addBotMessage("📱 Para dispositivo físico:")
            addBotMessage("   - Encuentra tu IP con 'ipconfig'")
            addBotMessage("   - Usa: http://TU_IP:8069")
            addBotMessage("   - Verifica que PC y móvil estén en la misma red WiFi")
        }
    }

    private fun sendToServer(userMsg: String) {
        Log.d(TAG, "Enviando mensaje: $userMsg")
        Log.d(TAG, "URL actual: $SERVER_URL")

        val payload = JSONObject().apply {
            put("from_phone", "3310222500")
            put("message", userMsg)
        }.toString()

        Log.d(TAG, "Payload: $payload")

        val body = payload.toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(SERVER_URL)
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build()

        sendButton.isEnabled = false
        addBotMessage("⏳ Enviando mensaje...")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Error de conexión con $SERVER_URL", e)
                activity?.runOnUiThread {
                    removeLoadingMessage()
                    addBotMessage("❌ Error: ${e.message}")

                    // Mostrar ayuda específica según el error
                    when {
                        e.message?.contains("ConnectException") == true -> {
                            addBotMessage("💡 El servidor no está disponible en esta IP")
                        }
                        e.message?.contains("SocketTimeoutException") == true -> {
                            addBotMessage("💡 Timeout - verifica firewall o red")
                        }
                        e.message?.contains("ECONNREFUSED") == true -> {
                            addBotMessage("💡 Conexión rechazada - verifica puerto")
                        }
                    }

                    sendButton.isEnabled = true
                }
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d(TAG, "Respuesta recibida: ${response.code} de $SERVER_URL")

                response.use {
                    activity?.runOnUiThread {
                        removeLoadingMessage()

                        if (it.isSuccessful) {
                            try {
                                val responseBody = it.body?.string() ?: ""
                                Log.d(TAG, "Respuesta: $responseBody")

                                val jsonResponse = JSONObject(responseBody)
                                val botResponse = when {
                                    jsonResponse.has("data") -> {
                                        val dataArray = jsonResponse.getJSONArray("data")
                                        if (dataArray.length() > 0) {
                                            dataArray.getString(0)
                                        } else "⚠️ Respuesta vacía del servidor"
                                    }
                                    jsonResponse.has("reply") -> jsonResponse.getString("reply")
                                    jsonResponse.has("message") -> jsonResponse.getString("message")
                                    else -> "✅ Respuesta: $responseBody"
                                }

                                addBotMessage(botResponse)

                            } catch (e: Exception) {
                                Log.e(TAG, "Error parseando respuesta", e)
                                addBotMessage("⚠️ Error procesando respuesta")
                            }
                        } else {
                            addBotMessage("⚠️ Error HTTP: ${it.code}")
                        }

                        sendButton.isEnabled = true
                    }
                }
            }
        })
    }

    private fun removeLoadingMessage() {
        if (messages.isNotEmpty() && messages.last().text.contains("⏳")) {
            messages.removeAt(messages.size - 1)
            chatAdapter.notifyItemRemoved(messages.size)
        }
    }

    private fun addUserMessage(msg: String) {
        messages.add(ChatMessage(msg, true))
        chatAdapter.notifyItemInserted(messages.size - 1)
        recyclerView.scrollToPosition(messages.size - 1)
    }

    private fun addBotMessage(msg: String) {
        messages.add(ChatMessage(msg, false))
        chatAdapter.notifyItemInserted(messages.size - 1)
        recyclerView.scrollToPosition(messages.size - 1)
    }
}

data class ChatMessage(val text: String, val isUser: Boolean)

class ChatAdapter(private val items: List<ChatMessage>) : RecyclerView.Adapter<ChatAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val text: TextView = view.findViewById(R.id.textMessage)
    }

    override fun getItemViewType(position: Int) =
        if (items[position].isUser) R.layout.item_message_user
        else R.layout.item_message_bot

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.text.text = items[position].text
    }

    override fun getItemCount() = items.size
}