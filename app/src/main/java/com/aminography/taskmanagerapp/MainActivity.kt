package com.aminography.taskmanagerapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = "${javaClass.simpleName} [taskId: $taskId]"

        val observer = object : LifecycleObserver {

            @OnLifecycleEvent(Lifecycle.Event.ON_START)
            fun onStart() {
                showToast("Task containing `ActivityA` comes to foreground!")
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            fun onStop() {
                showToast("Task containing `ActivityA` goes to background!")
            }
        }

        TaskLifecycleOwner.getInstance(applicationContext, ActivityA::class.java)
            .lifecycle
            .addObserver(observer)

        findViewById<Button>(R.id.button1).setOnClickListener {
            Intent(applicationContext, ActivityA::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }.let { applicationContext.startActivity(it) }
        }

        findViewById<Button>(R.id.button2).setOnClickListener {
            startActivity(Intent(applicationContext, ActivityB::class.java))
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }
}
