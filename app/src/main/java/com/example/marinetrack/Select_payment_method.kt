
package com.example.marinetrack

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Select_payment_method : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.select_payment_method)

        val bankCardPayButton: Button = findViewById(R.id.bank_card_pay)
        val bankSlipButton: Button = findViewById(R.id.bank_slip)

//        bankCardPayButton.setOnClickListener {
//            Toast.makeText(this, "Redirecting to Bank Card Payment", Toast.LENGTH_SHORT).show()
//            val intent = Intent(this, Card_payment::class.java)
//            startActivity(intent)
//        }
//
//        bankSlipButton.setOnClickListener {
//            Toast.makeText(this, "Opening Bank Slip", Toast.LENGTH_SHORT).show()
//            val intent = Intent(this, Slip_payment::class.java)
//            startActivity(intent)
//        }
    }
}
