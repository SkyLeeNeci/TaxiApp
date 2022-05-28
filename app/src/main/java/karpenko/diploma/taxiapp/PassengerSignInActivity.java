package karpenko.diploma.taxiapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class PassengerSignInActivity extends AppCompatActivity {

    public static final String TAG = "DriverSignInActivity";


    private TextInputLayout emailInputLayout;
    private TextInputLayout nameInputLayout;
    private TextInputLayout passwordInputLayout;
    private TextInputLayout passwordConfirmInputLayout;

    private FirebaseAuth mAuth;

    private Button loginSignInButton;
    private TextView toggleLoginSignInTV;
    private boolean isLoginModeActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_sign_in);

        mAuth = FirebaseAuth.getInstance();

        emailInputLayout = findViewById(R.id.emailInputLayout);
        nameInputLayout = findViewById(R.id.nameInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        passwordConfirmInputLayout = findViewById(R.id.passwordConfirmInputLayout);
        loginSignInButton = findViewById(R.id.loginSignInButton);
        toggleLoginSignInTV = findViewById(R.id.toggleLoginSignInTextView);

        if (mAuth.getCurrentUser() != null){
            startActivity(new Intent(this, PassengerMapsActivity.class));
        }

        loginSignInButton.setOnClickListener(view -> {

            if (!validateEmail()  | !validatePassword()){
                return;
            }

            if (isLoginModeActive){
                signInWithEmailAndPassword(emailInputLayout.getEditText().getText().toString(),
                        passwordInputLayout.getEditText().getText().toString());
            }else {

                if (!validateEmail() | !validateName() | !validatePassword() | !validateConfirmPassword()){
                    return;
                }

                createUserWithEmailAndPassword(emailInputLayout.getEditText().getText().toString(),
                        passwordInputLayout.getEditText().getText().toString());
            }

        });

        toggleLoginSignInTV.setOnClickListener(view -> {
            if (isLoginModeActive){
                isLoginModeActive = false;
                loginSignInButton.setText("Sign Up");
                toggleLoginSignInTV.setText("Tap to Log In");
                passwordConfirmInputLayout.setVisibility(View.VISIBLE);
                nameInputLayout.setVisibility(View.VISIBLE);
            }else {
                isLoginModeActive = true;
                loginSignInButton.setText("Log In");
                toggleLoginSignInTV.setText("Tap to Sign Up");
                passwordConfirmInputLayout.setVisibility(View.GONE);
                nameInputLayout.setVisibility(View.GONE);
            }
        });
    }
    private boolean validateEmail(){

        String emailInput = emailInputLayout.getEditText().getText().toString().trim();
        if (emailInput.isEmpty()){
            emailInputLayout.setError("Email can`t be empty");
            return false;
        }else {
            emailInputLayout.setError("");
            return true;
        }

    }

    private boolean validateName(){

        String nameInput = nameInputLayout.getEditText().getText().toString().trim();
        if (nameInput.isEmpty()){
            nameInputLayout.setError("Name can`t be empty");
            return false;
        }else if (nameInput.length() > 16){
            nameInputLayout.setError("Name length must be less than 15");
            return false;
        }else {
            nameInputLayout.setError("");
            return true;
        }

    }

    private boolean validatePassword(){

        String passwordInput = passwordInputLayout.getEditText().getText().toString().trim();
        String passwordConfirmInput = passwordConfirmInputLayout.getEditText().getText().toString().trim();

        if (passwordInput.isEmpty()){
            passwordInputLayout.setError("Password can`t be empty");
            return false;
        }else if (passwordInput.length() < 7){
            passwordInputLayout.setError("Password length can`t be less than 6");
            return false;
        }else {
            passwordInputLayout.setError("");
            return true;
        }

    }

    private boolean validateConfirmPassword(){

        String passwordInput = passwordInputLayout.getEditText().getText().toString().trim();
        String passwordConfirmInput = passwordConfirmInputLayout.getEditText().getText().toString().trim();

        if (!passwordConfirmInput.equals(passwordInput)){
            passwordInputLayout.setError("Password don`t match!");
            return false;
        }else {
            passwordInputLayout.setError("");
            return true;
        }

    }

    private void createUserWithEmailAndPassword(String email, String password){

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(PassengerSignInActivity.this, "Success", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(PassengerSignInActivity.this, PassengerMapsActivity.class));
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(PassengerSignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }
                    }
                });

    }

    private void signInWithEmailAndPassword(String email, String password){

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(PassengerSignInActivity.this, "Success", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(PassengerSignInActivity.this, PassengerMapsActivity.class));
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(PassengerSignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }
                    }
                });

    }
}