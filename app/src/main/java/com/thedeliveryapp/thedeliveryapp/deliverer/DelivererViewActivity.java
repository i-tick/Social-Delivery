package com.thedeliveryapp.thedeliveryapp.deliverer;

import android.content.Intent;
import android.graphics.Color;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.google.android.gms.auth.api.Auth;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.thedeliveryapp.thedeliveryapp.R;
import com.thedeliveryapp.thedeliveryapp.check_connectivity.CheckConnectivityMain;
import com.thedeliveryapp.thedeliveryapp.check_connectivity.ConnectivityReceiver;
import com.thedeliveryapp.thedeliveryapp.login.LoginActivity;
import com.thedeliveryapp.thedeliveryapp.login.MainActivity;
import com.thedeliveryapp.thedeliveryapp.login.user_details.UserDetails;
import com.thedeliveryapp.thedeliveryapp.order_form.OrderForm;
import com.thedeliveryapp.thedeliveryapp.recyclerview.OrderViewHolder;
import com.thedeliveryapp.thedeliveryapp.recyclerview.RecyclerViewOrderAdapter;
import com.thedeliveryapp.thedeliveryapp.recyclerview.SwipeOrderUtil;
import com.thedeliveryapp.thedeliveryapp.recyclerview.UserOrderItemClickListener;
import com.thedeliveryapp.thedeliveryapp.recyclerview.UserOrderTouchListener;
import com.thedeliveryapp.thedeliveryapp.user.UserOrderDetailActivity;
import com.thedeliveryapp.thedeliveryapp.user.UserViewActivity;
import com.thedeliveryapp.thedeliveryapp.user.order.OrderData;

import java.util.ArrayList;
import java.util.List;

import static com.thedeliveryapp.thedeliveryapp.login.LoginActivity.mGoogleApiClient;

public class DelivererViewActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {


    MenuItem mPreviousMenuItem=null;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference root = FirebaseDatabase.getInstance().getReference(), forUserData,childOrders;

    private String userId;
    boolean isRefreshing  = false;
    private UserDetails userDetails = new UserDetails();

    NavigationView navigationView;
    View mHeaderView;
    Toolbar toolbar;


    TextView textViewUserName;
    TextView textViewEmail;
    boolean pending ;
    boolean active ;
    boolean finished ;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private DrawerLayout mDrawerLayout;
    public static RecyclerViewOrderAdapter adapter;
    public List<OrderData> orderList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deliverer_view);
        checkConnection();
        setUpToolBarAndActionBar();
        setUpNavigationView();
        setUpDrawerLayout();
        setDefaultFlags();
        setUpSwipeRefresh();
        setUpRecyclerView();

    }
    void setDefaultFlags() {
        finished = false;
        active = true;
        pending = true;
    }

    void setUpNavigationView() {
        navigationView = findViewById(R.id.nav_view_deliverer);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                // set item as selected to persist highlight
                menuItem.setCheckable(true);
                menuItem.setChecked(true);
                if (mPreviousMenuItem != null) {
                    mPreviousMenuItem.setChecked(false);
                }
                mPreviousMenuItem = menuItem;// close drawer when item is tapped
                mDrawerLayout.closeDrawers();

                int id = menuItem.getItemId();

                if(id == R.id.sign_out_deliverer) {
                    Toast.makeText(DelivererViewActivity.this,"You have been successfully logged out.", Toast.LENGTH_LONG).show();
                    signOut();
                }
                else if(id == R.id.all_orders_deliverer) {
                    setDefaultFlags();
                    toolbar.setTitle("All Orders");
                    refreshOrders();
                }
                else if(id == R.id.completed_deliverer) {
                    active = false;
                    pending = false;
                    finished = true;
                    toolbar.setTitle("Finished");
                    refreshOrders();

                }
                else if(id == R.id.active_deliverer) {
                    active = true;
                    pending = false;
                    finished = false;
                    toolbar.setTitle("Active");
                    refreshOrders();
                }
                else if(id == R.id.pending_deliverer) {
                    active = false;
                    pending = true;
                    finished = false;
                    toolbar.setTitle("Pending");
                    refreshOrders();
                }
                else if(id == R.id.use_as_user) {
                    startActivity(new Intent(DelivererViewActivity.this, UserViewActivity.class));
                    finish();
                }
                // Add code here to update the UI based on the item selected
                // For example, swap UI fragments here
                mDrawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

    }

    public void signOut() {
        auth = FirebaseAuth.getInstance();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
        auth.signOut();
        sendToLogin();
    }

    public void sendToLogin() {
        Intent loginIntent = new Intent(DelivererViewActivity.this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }

    void setUpDrawerLayout() {
        mDrawerLayout = findViewById(R.id.drawer_layout_deliverer);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(DelivererViewActivity.this, mDrawerLayout, toolbar, R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mDrawerLayout.addDrawerListener(
            new DrawerLayout.DrawerListener() {
                @Override
                public void onDrawerSlide(View drawerView, float slideOffset) {
                    // Respond when the drawer's position changes
                    userId = user.getUid();

                    forUserData = root.child("deliveryApp").child("users").child(userId);
                    forUserData.keepSynced(true);
                    forUserData.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {


                            userDetails = dataSnapshot.getValue(UserDetails.class);
                            mHeaderView = navigationView.getHeaderView(0);

                            textViewUserName = mHeaderView.findViewById(R.id.headerUserName);
                            textViewEmail = mHeaderView.findViewById(R.id.headerUserEmail);
                            int wallet = userDetails.wallet;
                            ImageView walletBalance =  mHeaderView.findViewById(R.id.walletBalance);
                            TextDrawable drawable = TextDrawable.builder().beginConfig().textColor(Color.BLACK).bold().endConfig().buildRoundRect(Integer.toString(wallet),Color.WHITE,100);
                            walletBalance.setImageDrawable(drawable);

                            textViewUserName.setText(userDetails.name);
                            textViewEmail.setText(userDetails.Email);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }

                @Override
                public void onDrawerOpened(View drawerView) {
                    // Respond when the drawer is opened
                }

                @Override
                public void onDrawerClosed(View drawerView) {
                    // Respond when the drawer is closed
                }

                @Override
                public void onDrawerStateChanged(int newState) {
                    // Respond when the drawer motion state changes
                }
            }
        );

    }
    void setUpToolBarAndActionBar() {

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
        toolbar.setTitle(getTitle());

    }
    void setUpSwipeRefresh() {
        //Swipe Refresh Layout
        swipeRefreshLayout = findViewById(R.id.swiperefresh);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(isRefreshing) {
                    swipeRefreshLayout.setRefreshing(false);
                    return;
                }
                refreshOrders();
                if(swipeRefreshLayout.isRefreshing())
                    swipeRefreshLayout.setRefreshing(false);

            }


        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        refreshOrders();
        CheckConnectivityMain.getInstance().setConnectivityListener(DelivererViewActivity.this);
    }

    void setUpRecyclerView() {

        recyclerView = findViewById(R.id.item_list);
        orderList = new ArrayList<OrderData>();
        adapter = new RecyclerViewOrderAdapter(orderList, getApplication());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // animation
        RecyclerView.ItemAnimator itemAnimator = new
                DefaultItemAnimator();
        itemAnimator.setAddDuration(1000);
        itemAnimator.setRemoveDuration(1000);
        recyclerView.setItemAnimator(itemAnimator);

        recyclerView.addOnItemTouchListener(new UserOrderTouchListener(this, recyclerView, new UserOrderItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                OrderViewHolder viewHolder = (OrderViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
                if(viewHolder != null && !viewHolder.isClickable)
                    return;
                OrderData clickedOrder = orderList.get(position);
                Intent intent = new Intent(DelivererViewActivity.this,DelivererOrderDetailActivity.class);
                intent.putExtra("MyOrder",(Parcelable) clickedOrder);
                startActivity(intent);

            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));


    }

    void refreshOrders() {
        //TODO Add internet connectivity error
        final ProgressBar progressBar = findViewById(R.id.progressBarUserOrder);
        progressBar.setVisibility(View.VISIBLE);
        isRefreshing = true;
        userId = user.getUid();

        final int size = orderList.size();
        if (size>0) {
            for(int i = 0;i < size;i++) {
                adapter.remove(0);
            }
        }

        DatabaseReference allorders = root.child("deliveryApp").child("orders");
        allorders.keepSynced(true);
        allorders.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                isRefreshing = true;
                for (DataSnapshot userdata : dataSnapshot.getChildren()) {
                    if (userdata.getKey().equals(userId)) {
                        continue;
                    }

                    for (DataSnapshot orderdata : userdata.getChildren()) {
                        OrderData order = orderdata.getValue(OrderData.class);
                        if ((order.status.equals("PENDING") && pending) ||
                                (order.status.equals("ACTIVE") && active && userId.equals(order.acceptedBy.delivererID)) ||
                                (order.status.equals("FINISHED") && finished && userId.equals(order.acceptedBy.delivererID)))
                            adapter.insert(0, order);
                        //   Toast.makeText(ItemListActivity.this,Integer.toString(order.max_range), Toast.LENGTH_SHORT).show();
                        //Toast.makeText(ItemListActivity.this,Integer.toString(adapter.getItemCount()), Toast.LENGTH_LONG).show();
                    }
                }

                isRefreshing = false;
                progressBar.setVisibility(View.GONE);
            }



            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
    });




    }


    @Override
    protected void onStart() {
        super.onStart();
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        DrawerLayout drawer = findViewById(R.id.drawer_layout_deliverer);
        if(drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else {
            finish();
        }
    }

    private void checkConnection() {
        boolean isConnected = ConnectivityReceiver.isConnected();
        if(!isConnected)
            showSnack(isConnected);
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

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        showSnack(isConnected);
    }


}