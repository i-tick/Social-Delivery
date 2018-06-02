package com.thedeliveryapp.thedeliveryapp.order_form;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.thedeliveryapp.thedeliveryapp.R;
import com.thedeliveryapp.thedeliveryapp.user.UserOrderDetailActivity;
import com.thedeliveryapp.thedeliveryapp.user.order.ExpiryDate;
import com.thedeliveryapp.thedeliveryapp.user.order.ExpiryTime;
import com.thedeliveryapp.thedeliveryapp.user.order.OrderData;
import com.thedeliveryapp.thedeliveryapp.user.order.UserLocation;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class EditOrderForm extends AppCompatActivity {


    private DatabaseReference root;
    private DatabaseReference order;
    private String userId;
    private int OrderNumber, i, i1, year, monthOfYear, dayOfMonth;

    TextView category ;
    Button date_picker, time_picker, user_location;;
    Calendar calendar ;
    EditText description, min_int_range, max_int_range, delivery_charge;
    OrderData updated_order, myOrder;
    UserLocation userLocation = null;
    ExpiryTime expiryTime = null;
    ExpiryDate expiryDate = null;

    int PLACE_PICKER_REQUEST =1;

    private String date, time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_order_form);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        toolbar.setTitle("Edit Order");
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        myOrder = intent.getParcelableExtra("MyOrder");

        description = findViewById(R.id.description_of_order);
        category = findViewById(R.id.btn_category);
        delivery_charge = findViewById(R.id.delivery_charge);
        min_int_range = findViewById(R.id.min_int);
        max_int_range = findViewById(R.id.max_int);
        date_picker = findViewById(R.id.btn_date_picker);
        time_picker = findViewById(R.id.btn_time_picker);
        user_location = findViewById(R.id.user_location);

        calendar = Calendar.getInstance();
        OrderNumber = myOrder.orderId;

        description.setText(myOrder.description);
        category.setText(myOrder.category);
        delivery_charge.setText(myOrder.deliveryCharge + "");
        min_int_range.setText(myOrder.min_range + "");
        max_int_range.setText(myOrder.max_range + "");

        userLocation = new UserLocation(myOrder.userLocation.Name, myOrder.userLocation.Location, myOrder.userLocation.PhoneNumber);

        year = myOrder.expiryDate.year;
        monthOfYear = myOrder.expiryDate.month;
        dayOfMonth = myOrder.expiryDate.day;

        expiryDate = new ExpiryDate(year,monthOfYear,dayOfMonth);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, monthOfYear);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        String date = DateFormat.getDateInstance(DateFormat.MEDIUM).format(calendar.getTime());
        date_picker.setText(date);

        i = myOrder.expiryTime.hour;
        i1 = myOrder.expiryTime.minute;

        expiryTime = new ExpiryTime(i,i1);
        calendar.set(Calendar.HOUR_OF_DAY, i);
        calendar.set(Calendar.MINUTE, i1);
        String time = DateFormat.getTimeInstance(DateFormat.SHORT).format(calendar.getTime());
        time_picker.setText(time);



        /*
        userLocationName.setText(myOrder.userLocation.Name);
        userLocationLocation.setText(myOrder.userLocation.Location);
        */

        category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> mcategories = new ArrayList<String>();
                mcategories.add("Food");
                mcategories.add("Medicine");
                mcategories.add("Household");
                mcategories.add("Electronics");
                mcategories.add("Toiletries");
                mcategories.add("Books");
                mcategories.add("Clothing");
                mcategories.add("Shoes");
                mcategories.add("Sports");
                mcategories.add("Games");
                mcategories.add("Others");
                //Create sequence of items
                final CharSequence[] Categories = mcategories.toArray(new String[mcategories.size()]);
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(EditOrderForm.this);
                dialogBuilder.setTitle("Choose Category");
                dialogBuilder.setItems(Categories, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        String selectedText = Categories[item].toString();  //Selected item in listview
                        category.setText(selectedText);
                    }
                });
                //Create alert dialog object via builder
                AlertDialog alertDialogObject = dialogBuilder.create();
                //Show the dialog
                alertDialogObject.show();
            }
        });

        date_picker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(EditOrderForm.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        expiryDate = new ExpiryDate(year,monthOfYear,dayOfMonth);
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, monthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        String date = DateFormat.getDateInstance(DateFormat.MEDIUM).format(calendar.getTime());
                        date_picker.setText(date);
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });

        time_picker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(EditOrderForm.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        expiryTime = new ExpiryTime(i,i1);
                        calendar.set(Calendar.HOUR_OF_DAY, i);
                        calendar.set(Calendar.MINUTE, i1);
                        String time = DateFormat.getTimeInstance(DateFormat.SHORT).format(calendar.getTime());
                        time_picker.setText(time);
                    }
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
                timePickerDialog.show();
            }
        });

        user_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(EditOrderForm.this), PLACE_PICKER_REQUEST);
                } catch (Exception e) {
                    // Log.e(TAG, e.getStackTrace().toString());
                }
            }
        });

    }

    protected void onActivityResult(int requestCode, int resultCode,Intent data) {
        if(requestCode == PLACE_PICKER_REQUEST) {
            if(resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(EditOrderForm.this,data);
                userLocation = new UserLocation(place.getName().toString(),place.getAddress().toString(),place.getPhoneNumber().toString());
                String toastMsg = String.format("Place: %s", place.getName());
                Toast.makeText(EditOrderForm.this, toastMsg, Toast.LENGTH_LONG).show();
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        final String order_description = description.getText().toString();
        final String order_category = category.getText().toString();
        final String order_delivery_charge = delivery_charge.getText().toString();
        final String order_min_range = min_int_range.getText().toString();
        final String order_max_range = max_int_range.getText().toString();

        //noinspection SimplifiableIfStatement

        if (id == R.id.action_save) {
            //Default text for date_picker = "ExpiryDate"
            //Default text for time_picker = "ExpiryTime"
            if(order_description.equals("") || order_category.equals("None") || order_delivery_charge.equals("") || order_min_range.equals("") || order_max_range.equals("")) {
                new AlertDialog.Builder(EditOrderForm.this)
                        .setMessage(getString(R.string.dialog_save))
                        .setPositiveButton(getString(R.string.dialog_ok), null)
                        .show();
                return true;
            }

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            userId = user.getUid();
            root = FirebaseDatabase.getInstance().getReference();
            order = root.child("deliveryApp").child("orders").child(userId).child(OrderNumber+"");

            order.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    updated_order = new OrderData(order_category, order_description, OrderNumber, Integer.parseInt(order_max_range), Integer.parseInt(order_min_range), userLocation, expiryDate, expiryTime, "PENDING", Integer.parseInt(order_delivery_charge),"------");
                    root.child("deliveryApp").child("orders").child(userId).child(Integer.toString(OrderNumber)).setValue(updated_order);
                    Intent intent = new Intent(EditOrderForm.this, UserOrderDetailActivity.class);
                    intent.putExtra("MyOrder",(Parcelable) updated_order);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        } else if (id==android.R.id.home) {
            Intent intent = new Intent(EditOrderForm.this, UserOrderDetailActivity.class);
            intent.putExtra("MyOrder",(Parcelable) myOrder);
            startActivity(intent);
            finish();

        }

        return super.onOptionsItemSelected(item);
    }
}
