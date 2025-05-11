package com.example.marinetrack

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class WeatherActivity : AppCompatActivity() {

    // Define view references
    private lateinit var currentDateTime: TextView
    private lateinit var weatherIcon: ImageView
    private lateinit var temperature: TextView
    private lateinit var weatherCondition: TextView
    private lateinit var feelsLike: TextView
    private lateinit var humidity: TextView
    private lateinit var windSpeed: TextView
    private lateinit var windDirection: TextView
    private lateinit var tideStatus: TextView
    private lateinit var tideTime: TextView
    private lateinit var safetyIcon: ImageView
    private lateinit var safetyStatus: TextView
    private lateinit var weatherLoading: View
    private lateinit var weatherError: TextView
    private lateinit var alertCard: CardView
    private lateinit var alertText: TextView

    // API details
    private val API_KEY = "0e3c8d7f0e4f4dd9bb4160626241211" // WeatherAPI.com key
    private val CITY = "Galle,LK"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather_dashboard)

        // Initialize views
        initViews()

        // Set current date and time
        updateDateTime()

        // Fetch weather data
        fetchWeatherData()
    }

    private fun initViews() {
        currentDateTime = findViewById(R.id.currentDateTime)
        weatherIcon = findViewById(R.id.weatherIcon)
        temperature = findViewById(R.id.temperature)
        weatherCondition = findViewById(R.id.weatherCondition)
        feelsLike = findViewById(R.id.feelsLike)
        humidity = findViewById(R.id.humidity)
        windSpeed = findViewById(R.id.windSpeed)
        windDirection = findViewById(R.id.windDirection)
        tideStatus = findViewById(R.id.tideStatus)
        tideTime = findViewById(R.id.tideTime)
        safetyIcon = findViewById(R.id.safetyIcon)
        safetyStatus = findViewById(R.id.safetyStatus)
        weatherLoading = findViewById(R.id.weatherLoading)
        weatherError = findViewById(R.id.weatherError)
        alertCard = findViewById(R.id.alertCard)
        alertText = findViewById(R.id.alertText)
    }

    private fun updateDateTime() {
        val dateFormat = SimpleDateFormat("MMMM d, yyyy | h:mm a", Locale.getDefault())
        currentDateTime.text = dateFormat.format(Date())
    }

    private fun fetchWeatherData() {
        // Show loading state
        weatherLoading.visibility = View.VISIBLE
        weatherError.visibility = View.GONE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Build URL for API request
                val url = "https://api.weatherapi.com/v1/forecast.json?key=$API_KEY&q=$CITY&days=1&aqi=yes&alerts=yes"

                // Make HTTP request
                val response = URL(url).readText()

                // Parse JSON response
                val weatherData = Gson().fromJson(response, WeatherResponse::class.java)

                // Update UI on main thread
                withContext(Dispatchers.Main) {
                    updateUI(weatherData)
                    weatherLoading.visibility = View.GONE
                }
            } catch (e: Exception) {
                // Handle errors
                withContext(Dispatchers.Main) {
                    weatherLoading.visibility = View.GONE
                    weatherError.visibility = View.VISIBLE
                    weatherError.text = "Error: ${e.message}"
                }
            }
        }
    }

    private fun updateUI(weatherData: WeatherResponse) {
        // Update temperature and conditions
        temperature.text = "${weatherData.current.temp_c}°C"
        weatherCondition.text = weatherData.current.condition.text
        feelsLike.text = "Feels like: ${weatherData.current.feelslike_c}°C"
        humidity.text = "Humidity: ${weatherData.current.humidity}%"

        // Update wind information
        windSpeed.text = "${weatherData.current.wind_kph} km/h"
        windDirection.text = "${weatherData.current.wind_dir} (${weatherData.current.wind_degree}°)"

        // Load weather icon
        Glide.with(this)
            .load("https:${weatherData.current.condition.icon}")
            .into(weatherIcon)

        // Calculate and display tide information (simplified)
        val tideInfo = calculateApproximateTide()
        tideStatus.text = tideInfo.first
        tideTime.text = tideInfo.second

        // Determine marine safety status
        val safetyInfo = determineMarineSafetyStatus(weatherData.current.wind_kph)
        safetyStatus.text = safetyInfo.first
        safetyStatus.setTextColor(resources.getColor(
            when (safetyInfo.second) {
                -1 -> R.color.danger
                0 -> R.color.warning
                else -> R.color.success
            }
        ))

//        // Set safety icon
//        safetyIcon.setImageResource(
//            when (safetyInfo.second) {
//                -1 -> R.drawable.ic_shield_warning
//                0 -> R.drawable.ic_shield_alert
//                else -> R.drawable.ic_shield_check
//            }
//        )

        // Check for alerts
        if (weatherData.alerts?.alert?.isNotEmpty() == true) {
            alertCard.visibility = View.VISIBLE
            alertText.text = weatherData.alerts.alert[0].headline
        } else {
            alertCard.visibility = View.GONE
        }
    }

    // Simplified tide calculation
    private fun calculateApproximateTide(): Pair<String, String> {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        // Simplified model - high tides at approximately 6AM and 6PM
        // Low tides at approximately 12PM and 12AM
        val tideStatus = when {
            (hour in 0..3) || (hour in 12..15) -> "Falling"
            (hour in 3..6) || (hour in 15..18) -> "Rising"
            (hour in 6..9) || (hour in 18..21) -> "Falling"
            else -> "Rising"
        }

        val tideTime = when {
            hour < 6 -> "High tide at 6:00 AM"
            hour < 12 -> "Low tide at 12:00 PM"
            hour < 18 -> "High tide at 6:00 PM"
            else -> "Low tide at 12:00 AM"
        }

        return Pair(tideStatus, tideTime)
    }

    // Determine marine safety status based on wind speed
    private fun determineMarineSafetyStatus(windSpeed: Double): Pair<String, Int> {
        return when {
            windSpeed > 40 -> Pair("Dangerous", -1) // Red
            windSpeed > 25 -> Pair("Caution", 0)    // Yellow
            else -> Pair("Favorable", 1)            // Green
        }
    }

    // Data classes for API response
    data class WeatherResponse(
        val location: Location,
        val current: Current,
        val forecast: Forecast,
        val alerts: Alerts?
    )

    data class Location(
        val name: String,
        val region: String,
        val country: String,
        val lat: Double,
        val lon: Double,
        val localtime: String
    )

    data class Current(
        val temp_c: Double,
        val feelslike_c: Double,
        val condition: Condition,
        val wind_kph: Double,
        val wind_degree: Int,
        val wind_dir: String,
        val humidity: Int
    )

    data class Condition(
        val text: String,
        val icon: String
    )

    data class Forecast(
        val forecastday: List<ForecastDay>
    )

    data class ForecastDay(
        val date: String,
        val day: Day,
        val astro: Astro
    )

    data class Day(
        val maxtemp_c: Double,
        val mintemp_c: Double
    )

    data class Astro(
        val sunrise: String,
        val sunset: String
    )

    data class Alerts(
        val alert: List<Alert>
    )

    data class Alert(
        val headline: String,
        val event: String
    )
}