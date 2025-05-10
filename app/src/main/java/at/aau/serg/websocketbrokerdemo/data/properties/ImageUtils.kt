package at.aau.serg.websocketbrokerdemo.data.properties

import android.content.Context

fun getDrawableIdFromName(imageName: String, context: Context): Int {
    return context.resources.getIdentifier(imageName, "drawable", context.packageName)
}
