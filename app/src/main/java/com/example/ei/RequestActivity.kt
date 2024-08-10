package com.example.ei

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class RequestActivity : AppCompatActivity() {

    private lateinit var urlEditText: EditText
    private lateinit var sendRequestButton: Button
    private lateinit var resultTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request)

        urlEditText = findViewById(R.id.urlEditText)
        sendRequestButton = findViewById(R.id.sendRequestButton)
        resultTextView = findViewById(R.id.resultTextView)

        sendRequestButton.setOnClickListener {
            makeRequest(urlEditText.text.toString())
        }
    }

    private fun makeRequest(url: String) {
        val queue = Volley.newRequestQueue(this)

        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                resultTextView.text = "Respuesta: $response"
            },
            { error ->
                resultTextView.text = "Error: ${error.message}"
            }
        )

        queue.add(stringRequest)
    }
}