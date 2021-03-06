package com.thedeliveryapp.thedeliveryapp.deliverer;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OneSignal;
import com.thedeliveryapp.thedeliveryapp.R;
import com.thedeliveryapp.thedeliveryapp.check_connectivity.ConnectivityReceiver;
import com.thedeliveryapp.thedeliveryapp.user.order.OrderData;

import org.json.JSONException;
import org.json.JSONObject;

public class Otp_screen extends AppCompatActivity {

    private EditText f1, f2, f3, f4, f5;
    private Button btn_mark_delivered;
    private String otp, final_price, userId;
    private DatabaseReference root, wallet_ref, deliverer_ref;
    private OrderData myOrder;
    private int final_price_int, balance;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_screen);
        Intent intent = getIntent();
        final_price = intent.getStringExtra("Final Price");
        final_price_int = Integer.parseInt(final_price);
        otp = intent.getStringExtra("OTP");
        myOrder = intent.getParcelableExtra("MyOrder");

        f1 = (EditText) findViewById(R.id.f1);
        f2 = (EditText) findViewById(R.id.f2);
        f3 = (EditText) findViewById(R.id.f3);
        f4 = (EditText) findViewById(R.id.f4);
        f5 = (EditText) findViewById(R.id.f5);
        btn_mark_delivered = (Button) findViewById(R.id.btn_mark_delivered);

        f1.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() != 0) {
                    f2.requestFocus();
                }
            }
        });

        f2.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() != 0) {
                    f3.requestFocus();
                } else {
                    f1.requestFocus();
                }
            }
        });

        f3.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() != 0) {
                    f4.requestFocus();
                } else {
                    f2.requestFocus();
                }
            }
        });

        f4.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() != 0) {
                    f5.requestFocus();
                } else {
                    f3.requestFocus();
                }
            }
        });

        f5.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() == 0) {
                    f4.requestFocus();
                }
            }
        });

        btn_mark_delivered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!ConnectivityReceiver.isConnected()) {
                    showSnack(false);
                } else {
                    String secret = f1.getText().toString() + f2.getText().toString() + f3.getText().toString()
                            + f4.getText().toString() + f5.getText().toString();
                    if (secret.equals(otp)) {
                        root = FirebaseDatabase.getInstance().getReference();
                        root.child("deliveryApp").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.hasChild("our_wallet")) {
                                    root.child("deliveryApp").child("our_wallet").setValue(0);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        root.child("deliveryApp").child("orders").child(myOrder.userId).child(Integer.toString(myOrder.orderId)).child("status").setValue("FINISHED");


                        wallet_ref = root.child("deliveryApp").child("users").child(myOrder.userId).child("wallet");
                        wallet_ref.keepSynced(true);

                        wallet_ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Integer wal_bal = dataSnapshot.getValue(Integer.class);
                                balance = wal_bal;
                                wallet_ref.setValue(balance+(myOrder.max_range-final_price_int));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        userId = user.getUid();

                        deliverer_ref = root.child("deliveryApp").child("users").child(userId).child("wallet");
                        deliverer_ref.keepSynced(true);

                        deliverer_ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Integer wal_bal = dataSnapshot.getValue(Integer.class);
                                balance = wal_bal;
                                deliverer_ref.setValue(balance+myOrder.deliveryCharge);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        Toast.makeText(Otp_screen.this, "Delivery Finished!!", Toast.LENGTH_LONG).show();
                        //TODO display a congrats 'you just delivered a order screen'
                        setUpDeliveredNotif(myOrder);
                        Intent intent = new Intent(Otp_screen.this, DelivererViewActivity.class);
                        startActivity(intent);
                        finish();

                    } else {
                        Toast.makeText(Otp_screen.this, "Wrong OTP! Enter correct OTP", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });


    }

    // Showing the status in Snackbar
    private void showSnack(boolean isConnected) {
        String message;
        int color;
        if (isConnected) {
            message = "Good! Connected to Internet";
            color = Color.WHITE;
        } else {
            message = "Sorry! Not connected to internet";
            color = Color.RED;
        }

        Snackbar snackbar = Snackbar
                .make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG);

        View sbView = snackbar.getView();
        TextView textView = sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(color);
        snackbar.show();
    }

    public void setUpDeliveredNotif(final OrderData order) {
        String userId = order.userId;
        root.child("deliveryApp").child("users").child(userId).child("playerId").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String player_id = dataSnapshot.getValue(String.class);
                //TOAST
                try {
                    JSONObject notificationContent = new JSONObject("{'contents': {'en': '" + order.description + "'}," +
                            "'include_player_ids': ['" + player_id + "'], " +
                            "'headings': {'en': 'Your Order got delivered\nOrder Id : "+order.orderId+" '} " +
                            "}");
                    JSONObject order = new JSONObject();
                    order.put("userId", myOrder.userId);
                    order.put("orderId", myOrder.orderId);
                    notificationContent.putOpt("data", order);
                    OneSignal.postNotification(notificationContent, null);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
