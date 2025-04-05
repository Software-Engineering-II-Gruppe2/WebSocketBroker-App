package at.aau.serg.websocketbrokerdemo.ui
import java.net.HttpURLConnection
import java.net.URL
import java.io.InputStreamReader
import java.io.BufferedReader

class UserSettingsAPI {

    private val baseUrl = "http://localhost:8080/api/user-settings"


     fun getAvailableSounds(): List<String> {
        val url = URL("$baseUrl/sounds")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        return try {

            val inputStreamReader = InputStreamReader(connection.inputStream)
            val bufferedReader = BufferedReader(inputStreamReader)
            val response = bufferedReader.readText()


            response.split(",").map { it.trim() }
        } catch (e: Exception) {

            listOf()
        } finally {
            connection.disconnect()
        }
    }


     fun setUserSound(sound: String): String {
        val url = URL("$baseUrl/sound")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        connection.doOutput = true

        val postData = "sound=$sound"

        return try {
            connection.outputStream.write(postData.toByteArray())


            val inputStreamReader = InputStreamReader(connection.inputStream)
            val bufferedReader = BufferedReader(inputStreamReader)
            val response = bufferedReader.readText()

            response
        } catch (e: Exception) {

            "Fehler bei der Anfrage"
        } finally {
            connection.disconnect()
        }
    }
}

