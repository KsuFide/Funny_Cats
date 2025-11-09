package com.example.funny_cats.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.funny_cats.R
import com.example.funny_cats.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        //  очищаем стек при каждом переходе
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeFragment -> {
                    navController.navigate(R.id.homeFragment)
                    true
                }
                R.id.breedsFragment -> {
                    // ПЕРЕЗАПУСКАЕМ фрагмент каждый раз
                    navController.popBackStack(R.id.breedsFragment, true)
                    navController.navigate(R.id.breedsFragment)
                    true
                }
                R.id.favoritesFragment -> {
                    navController.navigate(R.id.favoritesFragment)
                    true
                }
                R.id.historyFragment -> {
                    navController.navigate(R.id.historyFragment)
                    true
                }
                R.id.settingsFragment -> {
                    navController.navigate(R.id.settingsFragment)
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
