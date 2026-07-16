package com.example.calc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.calc.data.DatabaseDriverFactory
import com.example.calc.data.SqlDelightHistoryRepository
import com.example.calc.data.createDatabase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = SqlDelightHistoryRepository(
            createDatabase(DatabaseDriverFactory(applicationContext)),
        )
        setContent { App(repository) }
    }
}
