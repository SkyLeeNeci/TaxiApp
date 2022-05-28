package karpenko.diploma.taxiapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class ChooseModeActivity extends AppCompatActivity {

    Button driverButton;
    Button clientButton;
    ImageView imageView;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_mode);

        imageView = findViewById(R.id.imageView);
        driverButton = findViewById(R.id.driverButton);
        clientButton = findViewById(R.id.clientButton);
        auth = FirebaseAuth.getInstance();



        imageView.setOnClickListener(view -> Toast.makeText(this, ")", Toast.LENGTH_SHORT).show());

        driverButton.setOnClickListener(view ->
                startActivity(new Intent(this,DriverSignInActivity.class)));

        clientButton.setOnClickListener(view ->
                startActivity(new Intent(this, PassengerSignInActivity.class)));
    }
}