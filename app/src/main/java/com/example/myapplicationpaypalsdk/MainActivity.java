package com.example.myapplicationpaypalsdk;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.braintreepayments.api.dropin.DropInActivity;
import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.DropInResult;
import com.braintreepayments.api.interfaces.HttpResponseCallback;
import com.braintreepayments.api.internal.HttpClient;
import com.braintreepayments.api.models.PayPalRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


// Main activity, the first screen to see when the app is open
public class MainActivity extends AppCompatActivity {

    // I'm using a static ip for the server, as well as the files for getting the token
    // as the main and checkout files for the server configuration
    final static String IP_ADDRESS = "192.168.1.49";
    final static String get_token = "http://" + IP_ADDRESS + "/BraintreePayments/main.php";
    final static String send_payment_details = "http://" + IP_ADDRESS + "/BraintreePayments/checkout.php";

    private static final int REQUEST_CODE_PAYMENT = 1; //To check if the costumer info was recovered from PayPal site
    HashMap<String, String> paramHash; //To finalize transaction
    String token; // Token generated using Server
    final String currencyCODE = "USD"; //Only payments in USD
    String amount = ""; //Amount to pay

    // From layout
    Button buttonPay;
    EditText editAmount;
    CheckBox checkboxItem1;

    protected void onDestroy(){
        stopService(new Intent(this, HttpRequest.class));
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            buttonPay = (Button) findViewById(R.id.buttonPay);
            editAmount = (EditText) findViewById(R.id.editAmount);
            checkboxItem1 = (CheckBox) findViewById(R.id.checkboxItem1);

            new HttpRequest().execute(); //Get the token from the server

            buttonPay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBraintreeSubmit();
                }
            });
    }

    //To review checked / unchecked elements
    public void itemClicked(View v) {
        boolean checked = ((CheckBox) v).isChecked();
        double toPay, old_toPay;
        String amount_toPay = String.valueOf(editAmount.getText());
        toPay = old_toPay = Double.parseDouble(amount_toPay);
        // Check which checkbox was clicked
        switch(v.getId()) {
            case R.id.checkboxItem1:
                if (checked)
                    toPay = toPay + 1.10;
                else
                    toPay = toPay - 1.10;
                break;
            case R.id.checkboxItem2:
                if (checked)
                    toPay = toPay + 2;
                else
                    toPay = toPay - 2;
                break;
            case R.id.checkboxItem3:
                if (checked)
                    toPay = toPay + 3.50;
                else
                    toPay = toPay - 3.50;
                break;
        }
        // Indicate items added / removed
        if (old_toPay < toPay)
            Toast.makeText(this, "Item added!", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, "Item removed!", Toast.LENGTH_SHORT).show();
        // Enable / disable payment button
        if (toPay > 0)
            buttonPay.setEnabled(true);
        else
            buttonPay.setEnabled(false);
        // Update amount to pay
        editAmount.setText(String.format("%.2f",toPay));
        amount = String.format("%.2f",toPay);
    }

    // action when PAY button is clicked
    public void onBraintreeSubmit() {
        PayPalRequest paypalRequest = new PayPalRequest(amount)
                .currencyCode(currencyCODE)
                .intent(PayPalRequest.INTENT_SALE);
        DropInRequest dropInRequest = new DropInRequest()
                .paypalRequest(paypalRequest)
                .clientToken(token);
        startActivityForResult(dropInRequest.getIntent(this), REQUEST_CODE_PAYMENT);
    }


    // Activity after come back from PayPal site (after log in in PayPal)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PAYMENT) {
            if (resultCode == RESULT_OK) {
                DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                String paymentMethodNonce = result.getPaymentMethodNonce().getNonce();
                Log.d("mylog", "Result: " + paymentMethodNonce); //write to log
                paramHash = new HashMap<>(); // to make transaction
                paramHash.put("amount", amount);
                paramHash.put("nonce", paymentMethodNonce);
                sendPaymentDetails();
                // Go to next activity
                startActivity(new Intent(this, PaymentDetails.class)
                        .putExtra("PaymentAmount", amount)
                        .putExtra("PaymentCurrency", currencyCODE));
            } else if (resultCode == RESULT_CANCELED) {
                // the user canceled
                Toast.makeText(MainActivity.this, "Payment cancelled by user", Toast.LENGTH_LONG).show();
                Log.d("mylog", "user canceled"); //to write log
            } else {
                // handle errors here, an exception may be available in
                Exception error = (Exception) data.getSerializableExtra(DropInActivity.EXTRA_ERROR);
                Toast.makeText(MainActivity.this, "Something went wrong!!! Message: " + error.toString(), Toast.LENGTH_LONG).show();
                Log.d("mylog", "Error : " + error.toString()); //write to log
            }
        }
        else { Toast.makeText(MainActivity.this, "Something went wrong!!! Request failed ", Toast.LENGTH_LONG).show(); }
    }

    // Finalize transaction
    private void sendPaymentDetails() {
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, send_payment_details,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(response.contains("Successful"))
                            Toast.makeText(MainActivity.this, "Transaction successful", Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(MainActivity.this, "Transaction failed : " + response.toString(), Toast.LENGTH_LONG).show();
                        Log.d("mylog", "Final Response: " + response.toString()); //write to log
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("mylog", "Volley error : " + error.toString()); //write to log
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                if (paramHash == null)
                    return null;
                Map<String, String> params = new HashMap<>();
                for (String key : paramHash.keySet()) {
                    params.put(key, paramHash.get(key));
                    Log.d("mylog", "Key : " + key + " Value : " + paramHash.get(key)); //write to log
                }
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };
        queue.add(stringRequest);
    }

    // To get token when Main Activity is created
    private class HttpRequest extends AsyncTask {
        ProgressDialog progress;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = new ProgressDialog(MainActivity.this, android.R.style.Theme_DeviceDefault_Dialog);
            progress.setCancelable(false);
            progress.setMessage("We are contacting our servers for token, Please wait");
            progress.setTitle("Getting token");
            progress.show();
        }
        @Override
        protected Object doInBackground(Object[] objects) {
            HttpClient client = new HttpClient();
            client.get(get_token, new HttpResponseCallback() {
                @Override
                public void success(String responseBody) {
                    Log.d("mylog", responseBody);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Successfully got token", Toast.LENGTH_SHORT).show();
                        }
                    });
                    token = responseBody;
                }
                @Override
                public void failure(Exception exception) {
                    final Exception ex = exception;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Failed to get token: " + ex.toString(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
            return null;
        }
        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            progress.dismiss();
        }
    }

}