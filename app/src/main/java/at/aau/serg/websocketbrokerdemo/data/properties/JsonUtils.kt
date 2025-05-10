package at.aau.serg.websocketbrokerdemo.data.properties

import android.content.Context


fun loadJsonFromAssets(context: Context, fileName: String): String {
    return context.assets.open(fileName).bufferedReader().use { it.readText() }
}
