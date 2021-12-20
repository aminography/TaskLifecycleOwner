package com.aminography.taskmanagerapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class ActivityA : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_layout)
        title = "${javaClass.simpleName} [taskId: $taskId]"

        findViewById<Button>(R.id.button).apply {
            text = "Next: ${ActivityC::class.java.simpleName}"

            setOnClickListener { startActivity(Intent(applicationContext, ActivityC::class.java)) }
        }
    }
}
