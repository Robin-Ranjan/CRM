package com.scout.crm

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModel
import com.scout.crm.databinding.ActivityMainBinding
import com.scout.crm.viewModals.MainViewModal
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    companion object {
        const val CUSTOM_FINE_LOCATION_PERMISSION = android.Manifest.permission.ACCESS_FINE_LOCATION
        const val CUSTOM_COARSE_LOCATION_PERMISSION = android.Manifest.permission.ACCESS_COARSE_LOCATION
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val mainViewModal: MainViewModal by viewModels()
    private var updatePunchIn = true
    private var updatePunchOut = true

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[CUSTOM_FINE_LOCATION_PERMISSION] == true -> {
                // Precise location access granted.
                // You can start location updates here.
            }
            permissions[CUSTOM_COARSE_LOCATION_PERMISSION] == true -> {
                // Only approximate location access granted.
                // You can start location updates with coarse accuracy here.
            }
            else -> {
                // No location access granted.
                Toast.makeText(this, "Please Give Access", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        Utill.statusBarColor(R.color.status_bar_color, this)
        // Initialize the fusedLocationClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//        locationPermission()
        binding.punchOut.setOnClickListener {
            if (binding.punchInText.text.equals("Punch In")) {
                Toast.makeText(this@MainActivity, "Please first Punch In", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            } else if (binding.punchOutText.text.equals("Check Out")) {
                Toast.makeText(this@MainActivity, "You Have Already Punch Out", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            punchOutDialog()
        }
        binding.punchIn.setOnClickListener {
            setPunchInDatetime()
            fetchLocation()
        }
//        binding.textEd.setOnClickListener{
//            fetchLocation()
//        }
        observeViewModel()
        retrieveAndSetLocation()
    }

    private fun setPunchOutDatetime() {
        if (binding.punchOutText.text == "Punch Out") {
            mainViewModal.setPunchOutDetails(System.currentTimeMillis())
            binding.punchOutText.text = "Check Out"
            setPunchInOutDetails()
            updatePunchOut = false
            binding.punchOutImg.visibility = View.VISIBLE
            binding.punchOut.setCardBackgroundColor(resources.getColor(R.color.screen_background))
            binding.punchOutText.setTextColor(resources.getColor(R.color.black))
        }
    }

    private fun setPunchInDatetime() {
        if (binding.punchInText.text == "Punch In") {
            mainViewModal.setPunchInDetails(System.currentTimeMillis())
            binding.punchInText.text = "Check In"
            setPunchInOutDetails()
            updatePunchIn = false
            binding.punchInImg.visibility = View.VISIBLE
            binding.punchIn.setCardBackgroundColor(resources.getColor(R.color.screen_background))
            binding.punchInText.setTextColor(resources.getColor(R.color.black))
        } else {
            Toast.makeText(this@MainActivity, "You Have Already Punch In", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun observeViewModel() {
        mainViewModal.currentTime.observe(this, Observer { time ->
            if (updatePunchIn) {
                binding.punchInTime.text = time
            }
            if (updatePunchOut) {
                binding.punchOutTime.text = time
            }
        })

        mainViewModal.punchInTimeStamp.observe(this, Observer { timeStamp ->
            val formatter = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
            val formattedTime = formatter.format(Date(timeStamp))
            binding.punchInTime.text = formattedTime
        })
    }

    private fun setPunchInOutDetails() {
        val formatterDate = SimpleDateFormat("dd MMM", Locale.getDefault())
        val currentDate = formatterDate.format(Date())
        binding.punchOutDate.text = currentDate
        binding.punchInDate.text = currentDate
    }

    private fun punchOutDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Do You Like to Punch Out ?")
        builder.setPositiveButton("Yes") { dilog, which ->
            setPunchOutDatetime()
            dilog.dismiss()
            Toast.makeText(this, "successfully check Out ", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("No") { dilog, which ->
            dilog.dismiss()
        }
        builder.show()
    }
//    private fun locationPermission(){
//         val locationPermissionRequest  = registerForActivityResult(
//             ActivityResultContracts.RequestMultiplePermissions()
//         ){ permissions ->
//             when {
//                 permissions.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
//                     // Precise location access granted.
//                 }
//                 permissions.getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
//                     // Only approximate location access granted.
//                 }
//                 else -> {
//                     // No location access granted.
//                 }
//             }
//         }
//
//        // Trigger the permission request
//        locationPermissionRequest.launch(arrayOf(
//            android.Manifest.permission.ACCESS_FINE_LOCATION,
//            android.Manifest.permission.ACCESS_COARSE_LOCATION
//        ))
//    }

    private fun fetchLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request permissions if not already granted
            checkAndRequestLocationPermissions()
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    // Location fetched successfully
                    val latitude = location.latitude
                    val longitude = location.longitude

                    // Save the location (you can save it to SharedPreferences, a database, or elsewhere)
                    saveLocation(latitude, longitude)

                    // Set the location to EditTexts
                    binding.textEd.setText(latitude.toString())
                } else {
                    // Handle case where location is null
                }
            }
    }
    private fun saveLocation(latitude: Double, longitude: Double) {
        // Save the location to SharedPreferences as an example
        val sharedPreferences = getSharedPreferences("LocationPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("Latitude", latitude.toString())
        editor.putString("Longitude", longitude.toString())
        editor.apply()

        // Alternatively, you can save the location to a database or elsewhere
//        binding.textEd.setText(sharedPreferences.getString("Latitude","") + sharedPreferences.getString("Longitude",""))
    }

    private fun checkAndRequestLocationPermissions() {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            this, CUSTOM_FINE_LOCATION_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            this, CUSTOM_COARSE_LOCATION_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineLocationGranted || !coarseLocationGranted) {
            // Request permissions
            locationPermissionRequest.launch(
                arrayOf(CUSTOM_FINE_LOCATION_PERMISSION, CUSTOM_COARSE_LOCATION_PERMISSION)
            )
        }
    }

    private fun retrieveAndSetLocation() {
        val sharedPreferences = getSharedPreferences("LocationPrefs", MODE_PRIVATE)
        val latitude = sharedPreferences.getString("Latitude", "")
        val longitude = sharedPreferences.getString("Longitude", "")

        binding.textEd.setText(latitude)
        binding.textEd1.setText(longitude)
    }
}