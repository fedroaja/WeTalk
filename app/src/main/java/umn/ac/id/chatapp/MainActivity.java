package umn.ac.id.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {
    TextView tvRegister, tvForgetPassword;
    EditText etEmail, etPassword;
    Button btnLogin;

    FirebaseAuth auth;
    FirebaseUser firebaseUser;


   @Override
    protected void onStart() {
        super.onStart();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        //check if user is null
        if (firebaseUser != null){
            if (firebaseUser.isEmailVerified()){
                Intent intent = new Intent(MainActivity.this,ChatActivity.class);
                startActivity(intent);
                finish();
            }

        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        auth = FirebaseAuth.getInstance();

        tvRegister = (TextView) findViewById(R.id.creatAcc);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        tvForgetPassword = (TextView) findViewById(R.id.forgetPass);

        tvForgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent IntentForgetPassword = new Intent(MainActivity.this, ResetPasswordActivity.class);
                startActivity(IntentForgetPassword);
            }
        });

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent IntentRegister = new Intent(MainActivity.this , RegisterActivity.class);
                startActivity(IntentRegister);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txtEmail = etEmail.getText().toString();
                String txtPassword = etPassword.getText().toString();

                if (TextUtils.isEmpty(txtEmail) || TextUtils.isEmpty(txtPassword)){
                    Toast.makeText(MainActivity.this, "All fields are required ", Toast.LENGTH_SHORT).show();
                }else {
                    final ProgressDialog pd = new ProgressDialog(MainActivity.this);
                    pd.setMessage("Log in");
                    pd.show();
                    auth.signInWithEmailAndPassword(txtEmail,txtPassword)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()){
                                        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                                        if (firebaseUser.isEmailVerified()){
                                            pd.dismiss();
                                            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK );
                                            startActivity(intent);
                                            finish();
                                        }else {
                                            Toast.makeText(MainActivity.this, "Please Verify your email !", Toast.LENGTH_SHORT).show();
                                            pd.dismiss();
                                        }
                                    }else {
                                        Toast.makeText(MainActivity.this, "Authentication failed !", Toast.LENGTH_SHORT).show();
                                        pd.dismiss();
                                    }
                                }
                            });
                }
            }
        });

    }
}
