package com.example.weatherapp
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import org.json.JSONObject
import org.w3c.dom.Text
import java.lang.Exception
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private var cityId  = 0
    private lateinit var addressTxt: TextView
    private lateinit var updated_At: TextView
    private lateinit var statusTxt: TextView
    private lateinit var tempTxt: TextView
    private lateinit var temp_min: TextView
    private lateinit var temp_max: TextView
    private lateinit var sunriseTxt: TextView
    private lateinit var sunsetTxt: TextView
    private lateinit var windTxt: TextView
    private lateinit var pressureTxt: TextView
    private lateinit var humidityTxt: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Weather App")
        var input = EditText(this)
        input.setHint("Plz Enter your country Zip number?")
        input.inputType = InputType.TYPE_CLASS_NUMBER
builder.setView(input)
        builder.setPositiveButton("Go") { dialogInterface: DialogInterface, i: Int ->
            cityId = input.text.toString().toInt()
            requestAPI()
        }
        builder.show()
        addressTxt = findViewById(R.id.address)
        updated_At = findViewById(R.id.updated_at)
        statusTxt = findViewById(R.id.status)
        tempTxt = findViewById(R.id.temp)
        temp_min = findViewById(R.id.temp_min)
        temp_max = findViewById(R.id.temp_max)
        sunriseTxt = findViewById(R.id.sunrise)
        sunsetTxt = findViewById(R.id.sunset)
        windTxt = findViewById(R.id.wind)
        pressureTxt = findViewById(R.id.pressure)
        humidityTxt = findViewById(R.id.humidity)
       // requestAPI()
    }

    private fun requestAPI() {
        CoroutineScope(IO).launch {
            val data = async { fetchData() }.await()
            if (data.isNotEmpty()) {
                populateRV(data)
            } else {
                Log.d("MAIN", "Unable to get data")
            }
        }
    }

    private fun fetchData(): String {
        var response = ""
        try {
            response =
                URL("https://api.openweathermap.org/data/2.5/weather?zip=$cityId&units=metric&appid=e812abc2cc4df7d6a36d190e5d2db50a").readText()

        } catch (e: Exception) {
            Log.d("MAIN", "ISSUE: $e")
        }
        return response
    }

    private suspend fun populateRV(result: String) {
        withContext(Main) {

                val jsonObj = JSONObject(result)
                val main = jsonObj.getJSONObject("main")
                val sys = jsonObj.getJSONObject("sys")
                val wind = jsonObj.getJSONObject("wind")
                val weather = jsonObj.getJSONArray("weather").getJSONObject(0)

                val updatedAt: Long = jsonObj.getLong("dt")
                val updatedAtText =
                    "Updated at: " + SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(
                        Date(updatedAt * 1000)
                    )
                updated_At.text = updatedAtText
                val temp = main.getString("temp") + "°C"
                tempTxt.text = temp
                val tempMin = "Min Temp: " + main.getString("temp_min") + "°C"
                temp_min.text = tempMin
                val tempMax = "Max Temp: " + main.getString("temp_max") + "°C"
                temp_max.text = tempMax
                val pressure = main.getString("pressure")
                pressureTxt.text = pressure
                val humidity = main.getString("humidity")
                humidityTxt.text = humidity
                val sunrise: Long = sys.getLong("sunrise")
                sunriseTxt.text =
                    SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunrise * 1000))
                val sunset: Long = sys.getLong("sunset")
                sunsetTxt.text =
                    SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunset * 1000))
                val windSpeed = wind.getString("speed")
                windTxt.text = windSpeed
                val weatherDescription = weather.getString("description")
                statusTxt.text = weatherDescription.capitalize()
                val address = jsonObj.getString("name") + ", " + sys.getString("country")
                addressTxt.text = address



        }
    }
}
