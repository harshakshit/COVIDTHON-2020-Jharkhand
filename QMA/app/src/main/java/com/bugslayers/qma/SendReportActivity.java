package com.bugslayers.qma;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class SendReportActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST = 1888;
    private CircularImageView imageView;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private Location selectedLocation;

    private TextInputEditText bodyTempTv, additonalTv;
    private CheckBox eyesCb, breathingCb, coughCb;

    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    private FusedLocationProviderClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_report);

        requestLocationPermission();
        client = LocationServices.getFusedLocationProviderClient(this);

        bodyTempTv = findViewById(R.id.body_temp);
        additonalTv = findViewById(R.id.additional_info);
        eyesCb = findViewById(R.id.eyes_checkbox);
        breathingCb = findViewById(R.id.breathing_checkbox);
        coughCb = findViewById(R.id.cough_checkbox);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        imageView = findViewById(R.id.main_item_profile_pic);
        imageView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                }
                else
                {
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                }
            }
        });

        Button submitBtn = findViewById(R.id.submit_btn);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(bodyTempTv.getText().toString())){
                    bodyTempTv.setError("Enter your body temperature");
                    return;
                }

                if(imageView.getDrawable() == null){
                    Toast.makeText(SendReportActivity.this, "Please click a selfie", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(ActivityCompat.checkSelfPermission(SendReportActivity.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(SendReportActivity.this, "Please allow location permissions", Toast.LENGTH_SHORT).show();
                    return;
                }

                client.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if(location!=null){
                            selectedLocation = location;
                        }
                    }
                });

                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                String id = databaseReference.push().getKey();

                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                Date date = new Date();
                String dateText = formatter.format(date);
                formatter = new SimpleDateFormat("HH:mm:ss");
                String time = formatter.format(date);

                if(selectedLocation != null){
                    databaseReference.child("reportDetails").child(userId).child(id).child("bodyTemp").setValue(bodyTempTv.getText().toString());
                    databaseReference.child("reportDetails").child(userId).child(id).child("additionalInfo").setValue(additonalTv.getText().toString());
                    databaseReference.child("reportDetails").child(userId).child(id).child("breathingProblem").setValue(breathingCb.isChecked());
                    databaseReference.child("reportDetails").child(userId).child(id).child("dryCough").setValue(coughCb.isChecked());
                    databaseReference.child("reportDetails").child(userId).child(id).child("irritationInEyes").setValue(eyesCb.isChecked());
                    databaseReference.child("reportDetails").child(userId).child(id).child("date").setValue(dateText);
                    databaseReference.child("reportDetails").child(userId).child(id).child("time").setValue(time);
                    databaseReference.child("reportDetails").child(userId).child(id).child("selfieUrl").setValue(uploadPic(userId, id));
                    databaseReference.child("reportDetails").child(userId).child(id).child("latitude").setValue(selectedLocation.getLatitude());
                    databaseReference.child("reportDetails").child(userId).child(id).child("longitude").setValue(selectedLocation.getLongitude());

                    Dialog dialog = onCreateDialog();
                    dialog.show();
                    DisplayMetrics metrics = getResources().getDisplayMetrics();
                    int width = metrics.widthPixels;
                    int height = metrics.heightPixels;
                    dialog.getWindow().setLayout(width, height);
                    dialog.getWindow().setBackgroundDrawable(getResources().getDrawable(android.R.color.transparent));
                }
            }
        });
    }

    private void requestLocationPermission(){
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
            else
            {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(photo);
        }
    }

    private String uploadPic(String userId, String reportId){
        if(imageView.getDrawable() != null){
            FirebaseStorage storage = FirebaseStorage.getInstance();

            Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] bitmapdata = stream.toByteArray();

            String path = "reportDetails/" + userId + "/" + reportId + "/photo";
            StorageReference storageReference = storage.getReference(path);

            storageReference.putBytes(bitmapdata);

            return path;
        }

        return null;
    }

    private Dialog onCreateDialog(){
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.report_submitted_dialog_layout);

        Button doneBtn = dialog.findViewById(R.id.check_btn);
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        return dialog;
    }
}
