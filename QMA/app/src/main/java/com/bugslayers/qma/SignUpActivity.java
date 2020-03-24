package com.bugslayers.qma;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    TextInputEditText nameEt, emailEt, addressEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        final String phone = getIntent().getStringExtra("phone");

        nameEt = findViewById(R.id.name);
        emailEt = findViewById(R.id.email);
        addressEt = findViewById(R.id.address);

        Button submitBtn = findViewById(R.id.submit_btn);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                User user = new User();
                user.setName(nameEt.getText().toString());
                user.setEmail(emailEt.getText().toString());
                user.setAddress(addressEt.getText().toString());
                user.setPhoneNumber(phone);

                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                databaseReference.child("users").child(userId).setValue(user);

                startActivity(new Intent(SignUpActivity.this, MainActivity.class));
            }
        });
    }
}
