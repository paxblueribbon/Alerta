package me.paxana.cwnet.ui;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.AuthUI.IdpConfig;
import com.firebase.ui.auth.AuthUI.IdpConfig.Builder;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.Arrays;

import me.paxana.cwnet.R;

import static com.firebase.ui.auth.AuthUI.EMAIL_PROVIDER;
import static com.firebase.ui.auth.AuthUI.PHONE_VERIFICATION_PROVIDER;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener {

    private Button buttonRegister;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText nameEditText;
    private TextView loginTextView;

    private FirebaseAuth firebaseAuth;
    private static final int RC_SIGN_IN = 1312;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        firebaseAuth = FirebaseAuth.getInstance();

        buttonRegister = findViewById(R.id.registerButton);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        loginTextView = findViewById(R.id.loginTextview);
        nameEditText = findViewById(R.id.nameEditText);

        buttonRegister.setOnClickListener(this);
        loginTextView.setOnClickListener(this);

        if (firebaseAuth.getCurrentUser() != null) {
            //user already signed in
            finish();
        }
        else {
            startActivityForResult(AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(Arrays.asList(
                            new Builder(EMAIL_PROVIDER).build(),
                            new Builder(PHONE_VERIFICATION_PROVIDER).build(),
                            new Builder(AuthUI.GOOGLE_PROVIDER).build(),
                            new Builder(AuthUI.FACEBOOK_PROVIDER).build()
                            )

                    ).build(), RC_SIGN_IN);
        }
        }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN){
            if (resultCode == RESULT_OK) {
                //user successfully signed in
                finish();
            }
        }
    }

    private void registerUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        final String name = nameEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)){
            //email is empty
            Toast.makeText(this, "Email, A Required Field, Is Empty", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(password)){
            Toast.makeText(this, "Password, A Required Field, Is Empty", Toast.LENGTH_LONG).show();
            return;
            //password is empty
        }

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(SignupActivity.this, "User successfully created", Toast.LENGTH_LONG).show();
                        FirebaseUser user = firebaseAuth.getCurrentUser();

                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name).build();

                        if (user != null) {
                            user.updateProfile(profileUpdates);
                        }
                    }
                    else {
                        Toast.makeText(SignupActivity.this, "User successfully created", Toast.LENGTH_LONG).show();
                    }
                }
            });
    }


    @Override
    public void onClick(View view) {
        if (view == buttonRegister) {
            registerUser();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }

        if (view == loginTextView) {
            finish();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }
}
