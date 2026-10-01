package com.example.domain

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class SpeechTargetField {
    MASTER,        // Parses quantity, name, and/or command
    QUANTITY_ONLY, // Parses digits/numbers only
    NAME_ONLY      // Captures item name text
}

class SpeechRecognizerManager(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _currentTarget = MutableStateFlow(SpeechTargetField.MASTER)
    val currentTarget: StateFlow<SpeechTargetField> = _currentTarget.asStateFlow()

    private val _lastRecognizedText = MutableStateFlow<String?>(null)
    val lastRecognizedText: StateFlow<String?> = _lastRecognizedText.asStateFlow()

    private val _audioRms = MutableStateFlow(0f)
    val audioRms: StateFlow<Float> = _audioRms.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    var onSpeechResult: ((text: String, target: SpeechTargetField) -> Unit)? = null
    var onFallbackToSystemDialog: ((intent: Intent, target: SpeechTargetField) -> Unit)? = null

    fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    fun createSpeechIntent(): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-SA")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ar")
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, "ar")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "تحدث بالصنف والكمية (مثال: خمسة أكياس بر)...")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
    }

    private fun ensureRecognizerCreated() {
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                    }

                    override fun onBeginningOfSpeech() {
                        _isListening.value = true
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        _audioRms.value = rmsdB.coerceIn(0f, 10f)
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _isListening.value = false
                        _audioRms.value = 0f
                    }

                    override fun onError(error: Int) {
                        _isListening.value = false
                        _audioRms.value = 0f

                        // If error client or busy, reset recognizer so next push works
                        if (error == SpeechRecognizer.ERROR_CLIENT || error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
                            cleanupRecognizer()
                            // Try fallback if available
                            onFallbackToSystemDialog?.invoke(createSpeechIntent(), _currentTarget.value)
                            return
                        }

                        val message = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "لم يتم التقاط صوت واضح، أعد المحاولة"
                            SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                                "تعذر الاتصال بالشبكة لمعالجة الصوت"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "انتهت مهلة التحدث بدون صوت"
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "يرجى تفعيل صلاحية الميكروفون"
                            else -> null
                        }
                        if (message != null) {
                            _errorMessage.value = message
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        _audioRms.value = 0f
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()?.trim()
                        if (!text.isNullOrEmpty()) {
                            _lastRecognizedText.value = text
                            onSpeechResult?.invoke(text, _currentTarget.value)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val partialMatches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val partial = partialMatches?.firstOrNull()
                        if (!partial.isNullOrEmpty()) {
                            _lastRecognizedText.value = partial
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        }
    }

    /**
     * Called when the user presses and holds the mic button.
     */
    fun startListening(target: SpeechTargetField = SpeechTargetField.MASTER) {
        _currentTarget.value = target
        _errorMessage.value = null

        if (!isAvailable()) {
            val fallbackIntent = createSpeechIntent()
            if (onFallbackToSystemDialog != null) {
                onFallbackToSystemDialog?.invoke(fallbackIntent, target)
                return
            } else {
                _errorMessage.value = "خدمة التعرف الصوتي غير مثبتة على هذا الجهاز"
                return
            }
        }

        mainHandler.post {
            try {
                // If already listening, stop previous session first
                if (_isListening.value) {
                    speechRecognizer?.stopListening()
                }

                ensureRecognizerCreated()
                _isListening.value = true
                speechRecognizer?.startListening(createSpeechIntent())
            } catch (e: Exception) {
                _isListening.value = false
                cleanupRecognizer()
                onFallbackToSystemDialog?.invoke(createSpeechIntent(), target)
            }
        }
    }

    /**
     * Called when the user releases the mic button in Push-To-Talk mode.
     * CRITICAL: Must ONLY call stopListening() to finalize speech and deliver onResults.
     * DO NOT cancel or destroy here!
     */
    fun stopListening() {
        mainHandler.post {
            try {
                _isListening.value = false
                _audioRms.value = 0f
                speechRecognizer?.stopListening()
            } catch (_: Exception) {}
        }
    }

    /**
     * Direct submission of recognized text (from fallback dialog or simulated tests).
     */
    fun submitRecognizedText(text: String, target: SpeechTargetField = _currentTarget.value) {
        val clean = text.trim()
        if (clean.isNotEmpty()) {
            _lastRecognizedText.value = clean
            onSpeechResult?.invoke(clean, target)
        }
    }

    /**
     * Completely cleans up resources when destroying ViewModel or activity.
     */
    fun cleanupRecognizer() {
        mainHandler.post {
            try {
                _isListening.value = false
                _audioRms.value = 0f
                speechRecognizer?.stopListening()
                speechRecognizer?.cancel()
                speechRecognizer?.destroy()
                speechRecognizer = null
            } catch (_: Exception) {}
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
