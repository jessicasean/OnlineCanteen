package com.example.asus.onlinecanteen.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.asus.onlinecanteen.R;
import com.example.asus.onlinecanteen.adapter.DeleteProductAdapter;
import com.example.asus.onlinecanteen.model.Product;
import com.example.asus.onlinecanteen.model.Store;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EditProductActivity extends AppCompatActivity implements DeleteProductAdapter.DeleteClickHandler{

    private static final String TAG = EditProductActivity.class.getSimpleName();
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 999;

    ImageView imageView;
    Button button, submitbtn;
    private static final int PICK_IMAGE = 100;
    Uri imageUri;
    String profPicUrl;
    FirebaseAuth mAuth;
    FirebaseUser merchant;
    String choose;
    private ChildEventListener productEventListener;
    Spinner spinner;
    ArrayList<String> productArrayList;
    private DatabaseReference databaseStore ,databaseUsers, databaseProducts;

    //EditText
    EditText productName, productPrice, productQty;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_product);

        mAuth = FirebaseAuth.getInstance();
        merchant = mAuth.getCurrentUser();

        spinner = findViewById(R.id.spinner);
        imageView = findViewById(R.id.productimageinput);
        button =  findViewById(R.id.productbrowse);
        submitbtn = findViewById(R.id.producteditbtn);
        productName = findViewById(R.id.productnamefill);
        productPrice = findViewById(R.id.productpricefill);
        productQty = findViewById(R.id.productqtyfill);
        getSupportActionBar().setTitle("Edit Product");
        //Browse Image in Gallery & set as Profile Picture
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        //Submit data for Sign Up & Upload to Storage
        submitbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        });
        databaseUsers = FirebaseDatabase.getInstance().getReference("users");
        databaseProducts = FirebaseDatabase.getInstance().getReference("products");
        databaseStore = FirebaseDatabase.getInstance().getReference("store");
        productArrayList = new ArrayList<>();
        getProduct();

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                choose =  parent.getItemAtPosition(position).toString();
                getDataContent();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void openGallery(){
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == PICK_IMAGE){
            imageUri = data.getData();
            imageView.setImageURI(imageUri);
        }
    }

    private void getDataContent(){
        //Log.i(TAG,"TEST@@");
        DatabaseReference productDatabase= FirebaseDatabase.getInstance().getReference();

        productDatabase.child("products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Log.i(TAG,"TEST@3");
                for(DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                    Product product = productSnapshot.getValue(Product.class);
                    //Log.i(TAG,"TEST PRD "+ product.toString());
                    if (merchant.getUid().equals(product.getTokoId()) && product.getName().equals(choose.toString())) {
                        Log.i(TAG,"TEST "+product.getName() +" "+product.getPrice()+" "+product.getStock() );
                        productName.setText(product.getName().toString());
                        productQty.setText(product.getStock().toString());
                        productPrice.setText(product.getPrice().toString());


                        if(product.getImageUrl() != null) {
                            Glide.with(imageView.getContext())
                                    .load(product.getImageUrl())
                                    .into(imageView);
                        }
                        else imageView.setImageResource(R.drawable.logo3);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void getProduct(){

        if(productEventListener == null) {
            productEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        Product product = dataSnapshot.getValue(Product.class);
                        if(merchant.getUid().equals(product.getTokoId()))
                        {
                            productArrayList.add(product.getName().toString());

                        }


                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, productArrayList);
                    spinner.setAdapter(dataAdapter);
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

            databaseProducts.addChildEventListener(productEventListener);
        }


    }
    private void uploadImage() {

        Log.d(TAG, "Uploading...");
        final StorageReference profileImageRef = FirebaseStorage.getInstance().getReference("product/"+System.currentTimeMillis()+".jpg");
        if (imageUri!=null){
            profileImageRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    @SuppressWarnings("VisibleForTests") Uri downloadUrl =taskSnapshot.getDownloadUrl();
                    profPicUrl = downloadUrl.toString();
                    submitData();
                    Log.d(TAG, "Success in uploading");
                    // backToScreen();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(),"Image failed to upload",Toast.LENGTH_LONG).show();
                    // backToScreen();
                }
            });
        }
        else
            submitDatawithoutImage();
    }
    //To submit data
    private void submitData() {

        Log.i(TAG,"TEXT2 MASUK" );
        if(validateRegisterInfo())
        {

            Log.i(TAG,"TEXT2 MASUK LAGI" );
            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            final DatabaseReference reference = firebaseDatabase.getReference();
            Query query = reference.child("products").orderByChild("tokoId").equalTo(merchant.getUid());
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {


                    for(DataSnapshot productSnapshot : dataSnapshot.getChildren()) {

                        //DataSnapshot nodeDataSnapshot = dataSnapshot.getChildren().iterator().next();

                        Product product = productSnapshot.getValue(Product.class);
                        if (merchant.getUid().equals(product.getTokoId()) && product.getName().equals(choose.toString())) {
                            Log.i(TAG,"TEXT2 test "+ productSnapshot.getValue().toString() );
                                //Log.i(TAG, "TEXT2 test " + nodeDataSnapshot.getValue().toString());
                                //String key = nodeDataSnapshot.getKey();
                                //String path = "/" + dataSnapshot.getKey() + "/" + key;
                                HashMap<String, Object> result = new HashMap<>();
                                //result.put("imageUrl", );
                                result.put("name", productName.getText().toString());
                                // HashMap<Integer, Object> result2 = new HashMap<>();
                                result.put("price", Integer.parseInt(productPrice.getText().toString()));
                                result.put("stock", Integer.parseInt(productQty.getText().toString()));
                                result.put("imageUrl", profPicUrl);

                                if(product.getImageUrl()!=null)
                                {
                                    StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(product.getImageUrl());

                                photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        // File deleted successfully
                                        Log.d(TAG, "onSuccess: deleted file");
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        // Uh-oh, an error occurred!
                                        Log.d(TAG, "onFailure: did not delete file");
                                    }
                                });
                                }
                                //if(!product.getImageUrl().equals())

                                reference.child("products").child(productSnapshot.getKey()).updateChildren(result);

                        }
                }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    //Logger.error(TAG, ">>> Error:" + "find onCancelled:" + databaseError);

                }
            });
            backToScreen();
        }

    }

    private void submitDatawithoutImage() {

        Log.i(TAG,"TEXT2 MASUK" );
        if(validateRegisterInfo())
        {

            Log.i(TAG,"TEXT2 MASUK LAGI" );
            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            final DatabaseReference reference = firebaseDatabase.getReference();
            Query query = reference.child("products").orderByChild("tokoId").equalTo(merchant.getUid());
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {


                    for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {

                        //DataSnapshot nodeDataSnapshot = dataSnapshot.getChildren().iterator().next();

                        Product product = productSnapshot.getValue(Product.class);
                        if (merchant.getUid().equals(product.getTokoId()) && product.getName().equals(choose.toString())) {
                            Log.i(TAG,"TEXT2 test "+ productSnapshot.getValue().toString() );
                            //String key = nodeDataSnapshot.getKey();
                            //String path = "/" + dataSnapshot.getKey() + "/" + key;
                            HashMap<String, Object> result = new HashMap<>();
                            //result.put("imageUrl", );
                            result.put("name", productName.getText().toString());
                            // HashMap<Integer, Object> result2 = new HashMap<>();
                            result.put("price", Integer.parseInt(productPrice.getText().toString()));
                            result.put("stock", Integer.parseInt(productQty.getText().toString()));

                            //if(!product.getImageUrl().equals())
                            reference.child("products").child(productSnapshot.getKey()).updateChildren(result);

                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    //Logger.error(TAG, ">>> Error:" + "find onCancelled:" + databaseError);

                }
            });
            backToScreen();
        }

    }

    private boolean validateRegisterInfo() {
        boolean valid = true;

        String productname = productName.getText().toString();
        if(TextUtils.isEmpty(productname)) {
            productName.setError("product name required");
            valid = false;
        } else {
            productName.setError(null);
        }

        String price = productPrice.getText().toString();
        if(TextUtils.isEmpty(price)) {
            productPrice.setError("price required");
            valid = false;
        } else {
            productPrice.setError(null);
        }

        String qty = productQty.getText().toString();
        if(TextUtils.isEmpty(qty)) {
            productQty.setError("quantity required");
            valid = false;
        } else {
            productQty.setError(null);
        }


        return valid;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_READ_EXTERNAL_STORAGE) {
            if(grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //addAdditionalUserInformation();
            }
        }
    }

    private void backToScreen() {
        finish();
    }

    @Override
    public void onClickHandler(Product product) {

    }
}
