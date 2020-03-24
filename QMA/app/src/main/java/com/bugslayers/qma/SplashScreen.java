package com.bugslayers.qma;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public class SplashScreen extends AppCompatActivity {

    private TextInputEditText inputPhone, inputCode;
    private FirebaseAuth auth;
    private Button sendOtpBtn, logInBtn;
    private String codeSent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null){
            startActivity(new Intent(SplashScreen.this,  MainActivity.class));
            finish();
        }

        inputPhone = findViewById(R.id.phone_number);
        inputCode = findViewById(R.id.otp);

        sendOtpBtn = findViewById(R.id.send_otp_btn);
        sendOtpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendVerificationCode();
                closeKeyboard();
                inputCode.setVisibility(View.VISIBLE);
                sendOtpBtn.setVisibility(View.GONE);
            }
        });

        logInBtn = findViewById(R.id.login_btn);
        logInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verifySignInCode();
                closeKeyboard();
            }
        });
    }

    private void verifySignInCode() {
        String code = inputCode.getText().toString();
        if (code.length() != 6) {
            inputCode.setError("Enter a valid OTP");
            return;
        }

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeSent, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        final String phone = inputPhone.getText().toString();

        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            final String userId = auth.getCurrentUser().getUid();
                            final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
                            Query query = databaseReference;
                            query.orderByChild("phoneNumber").equalTo(phone).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()){
                                        startActivity(new Intent(SplashScreen.this, MainActivity.class));
                                        finish();
                                    }
                                    else{
                                        databaseReference.child(userId).child("phoneNumber").setValue(phone);
                                        startActivity(new Intent(SplashScreen.this, SignUpActivity.class).putExtra("phone", phone));
                                        finish();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                        else
                            Toast.makeText(SplashScreen.this, "Please retry again", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendVerificationCode() {

        String phone = inputPhone.getText().toString();

        if (phone.isEmpty()) {
            inputPhone.setError("Phone no. is required");
            inputPhone.requestFocus();
            inputCode.setVisibility(View.INVISIBLE);
            return;
        }

        if (phone.length() < 10) {
            inputPhone.setError("Enter valid phone number");
            inputPhone.requestFocus();
            inputCode.setVisibility(View.INVISIBLE);
            return;
        }

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + phone,
                60,
                TimeUnit.SECONDS,
                SplashScreen.this,
                mCallbacks
        );

        Log.e("code", "sent");

        inputCode.setVisibility(View.VISIBLE);
        sendOtpBtn.setVisibility(View.GONE);
        logInBtn.setVisibility(View.VISIBLE);
    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

        }

        @Override
        public void onVerificationFailed(FirebaseException e) {

        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            codeSent = s;
        }
    };


    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}
