package com.example.myapplicationpaypalsdk;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

// Second activity, screen after send pay to PayPal
public class PaymentDetails extends AppCompatActivity {

    TextView txtId, txtAmount, txtStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_details);

        txtId = (TextView)findViewById(R.id.textViewID);
        txtAmount = (TextView)findViewById(R.id.textViewAmount);
        txtStatus = (TextView)findViewById(R.id.textViewStatus);

        Intent intent = getIntent();

        try{
            String paymentAmount = new String(intent.getStringExtra("PaymentAmount"));
            String paymentCurrency = new String(intent.getStringExtra("PaymentCurrency"));
            showDetails(paymentAmount, paymentCurrency);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Show result
    private void showDetails(String paymentAmount, String paymentCurrency){
        try {
            txtId.setText("Payment ID");
            txtAmount.setText(paymentCurrency + " " + paymentAmount);
            txtStatus.setText("PAYMENT DONE");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}