package at.aau.serg.websocketbrokerdemo.data.properties

import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PropertyViewModel : ViewModel() {

    fun getProperties(context: Context): List<Property> {
        val json = loadJsonFromAssets(context, "propertyData.json")
        val gson = Gson()

        // Use a wrapper class that matches your JSON structure
        val wrapperType = object : TypeToken<PropertyJsonWrapper>() {}.type
        val parsedWrapper: PropertyJsonWrapper = gson.fromJson(json, wrapperType)

        val allProperties = mutableListOf<Property>()

        allProperties.addAll(parsedWrapper.properties)
        allProperties.addAll(parsedWrapper.trainStations)
        allProperties.addAll(parsedWrapper.utilities)

        return allProperties
    }
}
