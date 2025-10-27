package com.example.funny_cats.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.funny_cats.R
import com.example.funny_cats.databinding.ActivityMainBinding
import com.example.funny_cats.ui.breeds.BreedsFragment
import com.example.funny_cats.ui.favorites.FavoritesFragment
import com.example.funny_cats.ui.home.HomeFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val TAG = "MainActivityDebug"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "🚀 MainActivity starting...")

        try {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)
            Log.d(TAG, "✅ Layout inflated successfully")

            setupNavigation()

        } catch (e: Exception) {
            Log.e(TAG, "❌ Critical error: ${e.message}", e)
            showEmergencyUI()
        }
    }

    private fun setupNavigation() {
        try {
            // Получаем NavHostFragment
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController = navHostFragment.navController

            Log.d(TAG, "NavController found: $navController")

            // Связываем BottomNavigationView с NavController
            binding.bottomNavigation.setupWithNavController(navController)

            Log.d(TAG, "✅ Navigation setup completed successfully")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Navigation setup failed: ${e.message}", e)
            setupSimpleNavigationFallback()
        }
    }

    private fun setupSimpleNavigationFallback() {
        Log.d(TAG, "🔄 Setting up fallback navigation")
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeFragment -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment, HomeFragment())
                        .commit()
                    true
                }
                R.id.breedsFragment -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment, BreedsFragment())
                        .commit()
                    true
                }
                R.id.favoritesFragment -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment, FavoritesFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }
    }

    private fun showEmergencyUI() {
        val textView = android.widget.TextView(this).apply {
            text = "🐱 CatFinder\n\nТехнические работы\n\nСкоро вернемся!"
            textSize = 18f
            setPadding(50, 50, 50, 50)
            gravity = android.view.Gravity.CENTER
        }
        setContentView(textView)
    }
}