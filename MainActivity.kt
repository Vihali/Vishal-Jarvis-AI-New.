
package com.vishal.jarvis

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private lateinit var statusText: TextView
    private lateinit var inputText: EditText
    private lateinit var chatText: TextView

    private val speechLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val spoken = result.data
                    ?.getStringArrayListExtra(
                        RecognizerIntent.EXTRA_RESULTS
                    )?.firstOrNull()

                if (!spoken.isNullOrBlank()) {
                    inputText.setText(spoken)
                    processCommand(spoken)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this, this)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 30, 24, 24)
        }

        statusText = TextView(this).apply {
            text = "VISHAL JARVIS AI"
            textSize = 25f
        }

        chatText = TextView(this).apply {
            text = "নমস্কার! আমি Jarvis। কীভাবে সাহায্য করতে পারি?"
            textSize = 18f
            setPadding(0, 25, 0, 25)
        }

        inputText = EditText(this).apply {
            hint = "এখানে লিখুন বা বলুন..."
            textSize = 16f
        }

        val sendButton = Button(this).apply {
            text = "Send"
            setOnClickListener {
                processCommand(inputText.text.toString())
            }
        }

        val voiceButton = Button(this).apply {
            text = "🎙 কথা বলুন"
            setOnClickListener {
                startListening()
            }
        }

        layout.addView(statusText)
        layout.addView(chatText)
        layout.addView(inputText)
        layout.addView(sendButton)
        layout.addView(voiceButton)

        setContentView(layout)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(
                Locale.forLanguageTag("bn-IN")
            )

            if (result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts.setLanguage(Locale("en", "IN"))
            }
        }
    }

    private fun startListening() {
        val intent = Intent(
            RecognizerIntent.ACTION_RECOGNIZE_SPEECH
        ).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                "bn-IN"
            )
            putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                "আপনার কথা বলুন"
            )
        }

        try {
            speechLauncher.launch(intent)
        } catch (e: Exception) {
            speak("দুঃখিত, ভয়েস সার্ভিস চালু করা যাচ্ছে না।")
        }
    }

    private fun processCommand(command: String) {
        val message = command.trim()
        val lower = message.lowercase(Locale.ROOT)

        if (message.isEmpty()) return

        chatText.text = "আপনি: $message"

        val answer = when {
            lower.contains("কে তৈরি") ||
            lower.contains("কে বানিয়েছে") ||
            lower.contains("who created") -> {
                "আমাকে তৈরি করেছে Vishal।"
            }

            lower.contains("তোমার নাম") ||
            lower.contains("তুমি কে") -> {
                "আমার নাম Jarvis। আমি আপনার AI Assistant।"
            }

            lower.contains("হ্যালো") ||
            lower.contains("hello") ||
            lower.contains("নমস্কার") -> {
                "নমস্কার! বলুন, কীভাবে সাহায্য করতে পারি?"
            }

            lower.contains("সময়") -> {
                "এখন সময় ${java.text.SimpleDateFormat(
                    "hh:mm a",
                    Locale("bn", "IN")
                ).format(java.util.Date())}"
            }

            lower.contains("কল করো") ||
            lower.contains("ফোন করো") ||
            lower.contains("call") -> {
                val number = Regex("[0-9+]{8,15}")
                    .find(message)?.value

                if (number != null) {
                    openDialer(number)
                    "ফোনের Dialer খুলে দিয়েছি। কল করার আগে নম্বরটি যাচাই করুন।"
                } else {
                    "যে নম্বরে কল করতে চান, সেটি বলুন।"
                }
            }

            else -> {
                "আপনার কথা বুঝেছি। উন্নত AI উত্তর পেতে API সংযোগ করতে হবে।"
            }
        }

        chatText.text = "আপনি: $message\n\nJarvis: $answer"
        speak(answer)
    }

    private fun openDialer(number: String) {
        val intent = Intent(
            Intent.ACTION_DIAL
        ).apply {
            data = android.net.Uri.parse("tel:$number")
        }

        startActivity(intent)
    }

    private fun speak(text: String) {
        if (::tts.isInitialized) {
            tts.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "jarvis_speech"
            )
        }
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}
