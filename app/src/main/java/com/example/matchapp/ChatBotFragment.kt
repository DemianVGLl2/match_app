package com.example.matchapp

import android.os.Bundle
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

class ChatBotFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var closeButton: ImageButton
    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()
    private val client = OkHttpClient()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat_bot, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupRecyclerView()
        setupClickListeners()

        // Agregar mensaje de bienvenida
        addBotMessage("¡Hola! Soy el ChatBot de UP. ¿En qué puedo ayudarte?")
    }

    private fun initializeViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerMessages)
        messageInput = view.findViewById(R.id.editTextMessage)
        sendButton = view.findViewById(R.id.btnSend)
        closeButton = view.findViewById(R.id.btnClose)
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(messages)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = chatAdapter
        }
    }

    private fun setupClickListeners() {
        sendButton.setOnClickListener {
            sendMessage()
        }

        closeButton.setOnClickListener {
            closeChatBot()
        }

        messageInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else {
                false
            }
        }
    }

    private fun sendMessage() {
        val text = messageInput.text.toString().trim()
        if (text.isEmpty()) return

        addUserMessage(text)
        messageInput.text.clear()

        callUpChatApi(text)
    }

    private fun callUpChatApi(question: String) {
        val payload = JSONObject().put("message", question).toString()

        val body = payload.toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url("http://TU_SERVIDOR:PUERTO/api/chat") // ← Ajusta aquí
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    addBotMessage("Lo siento, ocurrió un error de conexión.")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val replyText = if (it.isSuccessful) {
                        JSONObject(it.body!!.string()).optString("reply", "Sin respuesta.")
                    } else {
                        "Lo siento, no obtuve respuesta."
                    }
                    activity?.runOnUiThread {
                        addBotMessage(replyText)
                    }
                }
            }
        })
    }

    private fun addUserMessage(message: String) {
        messages.add(ChatMessage(message, true))
        chatAdapter.notifyItemInserted(messages.size - 1)
        scrollToBottom()
    }

    private fun addBotMessage(message: String) {
        messages.add(ChatMessage(message, false))
        chatAdapter.notifyItemInserted(messages.size - 1)
        scrollToBottom()
    }

    private fun scrollToBottom() {
        if (messages.isNotEmpty()) {
            recyclerView.scrollToPosition(messages.size - 1)
        }
    }

    /* private fun simulateBotResponse(userMessage: String) {
        view?.postDelayed({
            val response = getBotResponse(userMessage)
            addBotMessage(response)
        }, 1000)
    }

    private fun getBotResponse(userMessage: String): String {
        val message = userMessage.lowercase()
        return when {
            message.contains("hola") || message.contains("hello") ->
                "¡Hola! ¿Cómo estás? ¿En qué puedo ayudarte?"

            message.contains("ayuda") || message.contains("help") ->
                "Puedo ayudarte con información sobre:\n• UP4U\n• Portal UP\n• Horarios\n• Servicios estudiantiles\n¿Qué necesitas?"

            message.contains("horario") || message.contains("hora") ->
                "Los horarios de atención son:\n📅 Lunes a Viernes: 7:00 AM - 9:00 PM\n📅 Sábados: 8:00 AM - 2:00 PM"

            message.contains("up4u") ->
                "UP4U es tu plataforma estudiantil donde puedes:\n• Ver calificaciones\n• Consultar horarios\n• Servicios académicos\n¿Necesitas ayuda para acceder?"

            message.contains("portal") ->
                "El Portal UP contiene:\n• Información académica\n• Trámites estudiantiles\n• Recursos digitales\n¿Qué buscas específicamente?"

            message.contains("calificaciones") || message.contains("notas") ->
                "Para consultar calificaciones:\n1. Ingresa a UP4U\n2. Ve a la sección 'Académico'\n3. Selecciona 'Calificaciones'"

            message.contains("biblioteca") ->
                "La biblioteca está disponible:\n📚 Lunes a Viernes: 7:00 AM - 10:00 PM\n📚 Sábados: 8:00 AM - 6:00 PM\n📚 Domingos: 10:00 AM - 6:00 PM"

            message.contains("cafeteria") || message.contains("comida") ->
                "La cafetería opera:\n🍽️ Lunes a Viernes: 7:00 AM - 8:00 PM\n🍽️ Sábados: 8:00 AM - 4:00 PM"

            message.contains("gracias") || message.contains("thanks") ->
                "¡De nada! Estoy aquí para ayudarte. ¿Hay algo más en lo que pueda asistirte?"

            message.contains("adios") || message.contains("bye") ->
                "¡Hasta luego! Que tengas un excelente día. 👋"

            else -> {
                val responses = listOf(
                    "Interesante pregunta. ¿Podrías ser más específico para ayudarte mejor?",
                    "No estoy seguro sobre eso. ¿Podrías reformular tu pregunta?",
                    "Hmm, no tengo información específica sobre eso. ¿Hay algo más en lo que pueda ayudarte?",
                    "¿Podrías darme más detalles sobre lo que necesitas?"
                )
                responses.random()
            }
        }
    } */

    private fun closeChatBot() {
        parentFragmentManager.popBackStack()
    }

    companion object {
        @JvmStatic
        fun newInstance() = ChatBotFragment()
    }
}

// Clase de datos para los mensajes
data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

// Adapter corregido para el RecyclerView
class ChatAdapter(private val messages: List<ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // CORRECCIÓN: Usar el ID correcto para el TextView del mensaje
        val message: TextView = view.findViewById(R.id.textMessage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layoutId = if (viewType == 0) R.layout.item_message_bot else R.layout.item_message_user
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.message.text = message.text
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isUser) 1 else 0
    }

    override fun getItemCount() = messages.size
}