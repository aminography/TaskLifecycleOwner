package com.aminography.taskmanagerapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ActivityC : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_layout)
        title = "${javaClass.simpleName} [taskId: $taskId]"

        findViewById<Button>(R.id.button).apply {
            text = "Next: ${ActivityD::class.java.simpleName}"

            setOnClickListener {
                Intent(applicationContext, ActivityD::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }.let { applicationContext.startActivity(it) }
            }
        }
    }
}
