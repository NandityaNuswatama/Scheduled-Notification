package com.example.timednotification

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setTheme(R.style.PreferenceScreen)

        supportFragmentManager.beginTransaction().add(R.id.settings_holder, SettingsPreference()).commit()
    }
}