package at.aau.serg.websocketbrokerdemo.logic

import android.content.Context
import android.media.MediaPlayer

class SoundPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null

    /**
     * Spielt eine Sounddatei aus dem res/raw Verzeichnis ab.
     * @param resId Die Ressourcen-ID der Sounddatei, z. B. R.raw.victory_sound
     */
    fun playSound(resId: Int) {
        // Vorherigen Player stoppen
        mediaPlayer?.release()

        // Neuen MediaPlayer erzeugen und starten
        mediaPlayer = MediaPlayer.create(context, resId).apply {
            setOnCompletionListener {
                it.release()
            }
            start()
        }
    }

    /**
     * Stoppt die Wiedergabe und gibt Ressourcen frei.
     */
    fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}