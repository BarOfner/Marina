package com.example.user.marina;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class LogInScreen extends AppCompatActivity {

    private Button SignInButton;
    private EditText PasswordBox;
    private EditText EmailBox;

    private FirebaseAuth mauth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in_screen);

        mauth = FirebaseAuth.getInstance();

        SignInButton = findViewById(R.id.SignInButton);
        PasswordBox = findViewById(R.id.PasswordBox);
        EmailBox = findViewById(R.id.EmailBox);

        SignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!PasswordBox.getText().toString().equals("") && !EmailBox.getText().toString().equals(""))
                {

                    if(mauth.getInstance().getCurrentUser() != null) {
                        mauth.getInstance().signOut();
                    }

                    mauth.getInstance().signInWithEmailAndPassword(EmailBox.getText().toString(), PasswordBox.getText().toString()).addOnCompleteListener(LogInScreen.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful())
                            {
                                LoadMainActivity();
                            }
                            else
                            {
                                Toast.makeText(LogInScreen.this, "Login failed! Invalid email or password,\nPlease try agin.", Toast.LENGTH_LONG).show();
                            }
                        }
                    });


                }

            }
        });
    }

    public void LoadMainActivity()
    {
        Intent i = new Intent(LogInScreen.this, MainActivity.class);
        startActivity(i);
        finish();
    }


    @Override
    public void onBackPressed() {
        LoadMainActivity();
    }
}
