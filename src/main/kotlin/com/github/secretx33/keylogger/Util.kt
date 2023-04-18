package com.github.secretx33.keylogger

import org.jnativehook.GlobalScreen
import org.jnativehook.keyboard.NativeKeyEvent
import java.nio.file.Path
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.exists
import kotlin.reflect.KProperty0
import kotlin.reflect.jvm.isAccessible
import kotlin.time.Duration

val IS_DEBUG_ENABLED = System.getenv("KEYLOGGER_DEBUG").toBoolean()

/**
 * Bye bye, annoying `info` logger from JNativeHook.
 */
fun disableAnnoyingJnativehookLogger() {
    // Get the logger for "org.jnativehook" and set the level to warning.
    val logger = Logger.getLogger(GlobalScreen::class.java.getPackage().name)
    logger.level = Level.WARNING

    // Don't forget to disable the parent handlers.
    logger.useParentHandlers = false
}

fun NativeKeyEvent.hasModifier(modifier: Int): Boolean = modifiers and modifier != 0

fun Path.createFileIfNotExists() {
    if (exists()) return
    parent?.createDirectories()
    createFile()
}

/**
 * Returns true if a lazy property reference has been initialized, or if the property is not lazy.
 */
val KProperty0<*>.isLazyInitialized: Boolean
    get() {
        if (this !is Lazy<*>) return true
        isAccessible = true
        return (getDelegate() as Lazy<*>).isInitialized()
    }

/**
 * Returns a string representation of the duration in seconds, with increased precision when the time is below one
 * second.
 *
 * @return a formatted string representation of the duration in seconds.
 */
fun Duration.formattedSeconds(): String {
    val secondsDouble = inWholeMilliseconds.toDouble() / 1000.0
    val pattern = when {
        inWholeSeconds <= 0 -> "#.##"
        else -> "#,###.#"
    }
    val format = DecimalFormat(pattern, DecimalFormatSymbols(Locale.US))
    return format.format(secondsDouble)
}