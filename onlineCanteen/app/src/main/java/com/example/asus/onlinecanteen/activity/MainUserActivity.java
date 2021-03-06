package com.example.asus.onlinecanteen.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.asus.onlinecanteen.R;
import com.example.asus.onlinecanteen.fragment.MainUserFragment;
import com.example.asus.onlinecanteen.model.Store;
import com.example.asus.onlinecanteen.model.User;
import com.example.asus.onlinecanteen.utils.AccountUtil;
import com.example.asus.onlinecanteen.utils.WalletUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class MainUserActivity extends AppCompatActivity implements MainUserFragment.StoreClickHandler, MainUserFragment.FeaturedProductClickHandler {

    private static final String TAG = MainUserActivity.class.getSimpleName();

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FragmentManager fragmentManager;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private DatabaseReference walletRef;
    private DatabaseReference userReference;
    private ChildEventListener userEventListener;

    private TextView username;
    private TextView email;
    private ImageView profilePicture;
    private TextView userWallet;

    private WalletUtil walletUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user);

        Log.d(TAG, "onCreate() called");

        // Set up customized toolbar
        Toolbar toolbar = findViewById(R.id.main_user_toolbar);
        setSupportActionBar(toolbar);
        // Add drawer toggle
        drawerLayout = findViewById(R.id.main_user_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Put default fragment
        MainUserFragment userFragment = new MainUserFragment();
        userFragment.setStoreClickHandler(this);
        userFragment.setFeaturedProductClickHandler(this);

        fragmentManager = getSupportFragmentManager();
        changeFragment(userFragment);

        // Set up navigation view
        navigationView = findViewById(R.id.main_user_navigation_view);
        navigationView.setNavigationItemSelectedListener(new MainNavigationListener());

        //Get User
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        //Set up header navigation
        NavigationView navigationView = findViewById(R.id.main_user_navigation_view);
        View header = navigationView.getHeaderView(0);
        userWallet = header.findViewById(R.id.user_wallet);
        username = header.findViewById(R.id.user_navigation_user_name);
        email = header.findViewById(R.id.user_navigation_user_email);
        profilePicture = header.findViewById(R.id.user_navigation_user_picture);

        //Get wallet amount
        walletRef = FirebaseDatabase.getInstance().getReference().child("wallet");
        walletRef.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    userWallet.setText(dataSnapshot.getValue().toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        userReference = FirebaseDatabase.getInstance().getReference("users");


        populateUserInfo();
    }

    private void populateUserInfo() {
        // Set username and email
        User currentUser = AccountUtil.getCurrentUser();
        email.setText(user.getEmail());
        if(currentUser != null) {
            username.setText(currentUser.getName());
            if (currentUser.getProfilePictureUrl() != null) {
                Glide.with(this)
                        .load(currentUser.getProfilePictureUrl())
                        .into(profilePicture);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (fragmentManager.getBackStackEntryCount() == 1) {
            finish();
        } else super.onBackPressed();
    }

    @Override
    public void storeClickHandler(Store store) {
        Intent intent = new Intent(MainUserActivity.this, UserStoreProductActivity.class);
        intent.putExtra(UserStoreProductActivity.CURRENT_STORE_KEY, store);
        startActivity(intent);
    }

    public void featuredProductClickHandler(Store store){
        Intent intent = new Intent(MainUserActivity.this, UserStoreProductActivity.class);
        intent.putExtra(UserStoreProductActivity.CURRENT_STORE_KEY, store);
        startActivity(intent);
        Log.i("adf", "adf" + store);
    }

    private void changeFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_user_frame_layout, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    //User Logout
    public void logout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.signOut_confirmation)
                .setCancelable(false)
                .setPositiveButton(R.string.signOut_confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        firebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(MainUserActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton(R.string.signOut_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.setTitle(R.string.signOut_title);
        alert.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        userEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.getKey().equals(user.getUid())) {
                    User currentUser = dataSnapshot.getValue(User.class);
                    AccountUtil.setCurrentAccount(currentUser);
                    populateUserInfo();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        if(userReference != null) userReference.addChildEventListener(userEventListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(userReference != null) userReference.removeEventListener(userEventListener);
    }

    private class MainNavigationListener implements NavigationView.OnNavigationItemSelectedListener {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            // Checked the item
            item.setChecked(true);

            // Close the drawer
            drawerLayout.closeDrawer(GravityCompat.START);

            switch (item.getItemId()) {
                case R.id.menu_order_item:
                    Intent currentIntent = new Intent(MainUserActivity.this, UserCurrentOrderActivity.class);
                    startActivity(currentIntent);
                    break;
                case R.id.menu_history_item:
                    Intent historyIntent = new Intent(MainUserActivity.this, UserHistoryActivity.class);
                    startActivity(historyIntent);
                    break;
                case R.id.navigation_menu_logout:
                    logout();
                    break;
                case R.id.navigation_menu_qr:
                    showQR();
                    break;
                case R.id.navigation_menu_top_up:
                    Intent topUpIntent = new Intent(MainUserActivity.this, RequestTopUpActivity.class);
                    startActivity(topUpIntent);
                    break;
                case R.id.navigation_menu_edit_profile:
                    Intent editProfileIntent = new Intent(MainUserActivity.this, EditUserProfileActivity.class);
                    startActivity(editProfileIntent);
                    break;
                default:
                    break;
            }
            return true;
        }
    }

    private void showQR() {
        Bitmap bitmap;
        // TODO Auto-generated method stub
        AlertDialog.Builder alertadd = new AlertDialog.Builder(
                this);
        alertadd.setTitle("QR Code");

        LayoutInflater factory = LayoutInflater.from(this);
        final View view = factory.inflate(R.layout.qr_layout, null);

        String text2Qr = user.getUid().toString();
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(text2Qr, BarcodeFormat.QR_CODE,300,300);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            bitmap = barcodeEncoder.createBitmap(bitMatrix);
            ImageView image= (ImageView) view.findViewById(R.id.imageView);
            image.setImageBitmap(bitmap);
        }  catch (WriterException e) {
            e.printStackTrace();
        }

        alertadd.setView(view);
        alertadd.setNeutralButton("Close", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dlg, int sumthin) {

            }
        });

        alertadd.show();

    }

    }

