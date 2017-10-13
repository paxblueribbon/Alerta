package me.paxana.cwnet.UI;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import me.paxana.cwnet.R;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener {

    private Button buttonRegister;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText nameEditText;
    private TextView loginTextView;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        firebaseAuth = FirebaseAuth.getInstance();

        buttonRegister = (Button) findViewById(R.id.registerButton);
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        loginTextView = (TextView) findViewById(R.id.loginTextview);
        nameEditText = (EditText) findViewById(R.id.nameEditText);

        buttonRegister.setOnClickListener(this);
        loginTextView.setOnClickListener(this);
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
                    //user is successfully registered
                    //start main activity
                    if (task.isSuccessful()) {
                        Toast.makeText(SignupActivity.this, "User successfully created", Toast.LENGTH_LONG).show();
                        FirebaseUser user = firebaseAuth.getCurrentUser();

                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name).build();

                        user.updateProfile(profileUpdates);



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
