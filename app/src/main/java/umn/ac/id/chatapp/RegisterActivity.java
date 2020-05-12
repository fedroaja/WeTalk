package umn.ac.id.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    EditText etRegisUsername,etRegisDisplayname,etRegisEmail,etRegisPassword,etRegisBirthDay;
    Button btnRegister;
    Calendar myCalendar;

    FirebaseAuth auth;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        this.getSupportActionBar().setTitle("Register");
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        myCalendar = Calendar.getInstance();

        etRegisUsername = (EditText) findViewById(R.id.etRegisUsername);
        etRegisDisplayname = (EditText) findViewById(R.id.etRegisDisplayname);
        etRegisEmail = (EditText) findViewById(R.id.etRegisEmail);
        etRegisPassword = (EditText) findViewById(R.id.etRegisPassword);
        etRegisBirthDay = (EditText) findViewById(R.id.Birthday);
        btnRegister = (Button) findViewById(R.id.btnRegister);

        auth = FirebaseAuth.getInstance();

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

        };

        etRegisBirthDay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(RegisterActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Pattern pattern = Pattern.compile("[a-zA-Z0-9]*");
                final String txtUsername = etRegisUsername.getText().toString().toLowerCase();
                final Matcher matcher = pattern.matcher(txtUsername);
                final String txtDisplayname = etRegisDisplayname.getText().toString();
                final String txtEmail = etRegisEmail.getText().toString();
                final String txtPassword = etRegisPassword.getText().toString();
                final String txtBirthday = etRegisBirthDay.getText().toString();
                Query usernameQuery = FirebaseDatabase.getInstance().getReference().child("Users").orderByChild("username").equalTo(txtUsername);
                usernameQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (TextUtils.isEmpty(txtUsername) || TextUtils.isEmpty(txtEmail) || TextUtils.isEmpty(txtPassword) || TextUtils.isEmpty(txtBirthday)){
                            Toast.makeText(RegisterActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                        }else if (txtPassword.length() < 6 ){
                            Toast.makeText(RegisterActivity.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                        }else if (txtUsername.length() < 3){
                            Toast.makeText(RegisterActivity.this, "Username must be at least 3 characters", Toast.LENGTH_SHORT).show();
                        }else if (dataSnapshot.getChildrenCount()>0){
                            Toast.makeText(RegisterActivity.this,"Choose a different username", Toast.LENGTH_SHORT).show();
                        }else if (!matcher.matches()) {
                            Toast.makeText(RegisterActivity.this,"Special character not allowed", Toast.LENGTH_SHORT).show();
                        }else{
                            register(txtUsername,txtDisplayname,txtEmail,txtPassword,txtBirthday);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }
        });
    }

    private void register(final String username,final String displayname, final String email, String password, final String tanggal){
        final ProgressDialog pd = new ProgressDialog(RegisterActivity.this);
        pd.setMessage("Create New Account");
        pd.show();
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){

                            //email verification
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            assert user != null;
                            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(RegisterActivity.this, "Pleas click Email verification in your email to Continue", Toast.LENGTH_SHORT).show();
                                    }else{
                                        Toast.makeText(RegisterActivity.this, "Something wrong, please make sure you fill all condition", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            assert firebaseUser != null;
                            String userid = firebaseUser.getUid();


                            reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("id", userid);
                            hashMap.put("username", username);
                            hashMap.put("displayname", displayname);
                            hashMap.put("birthday", tanggal);
                            hashMap.put("imageURL", "default");
                            hashMap.put("status","offline");
                            hashMap.put("time", ServerValue.TIMESTAMP);

                            reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(RegisterActivity.this, "Authentication completed.",
                                                Toast.LENGTH_SHORT).show();
                                        pd.dismiss();
                                        Intent intent = new Intent(RegisterActivity.this , MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            });
                        }else {
                            Toast.makeText(RegisterActivity.this, "Authentication failed:" +
                                    task.getException() + email, Toast.LENGTH_SHORT).show();
                            pd.dismiss();
                        }
                    }
                });
    }

    private void updateLabel() {
        String myFormat = "MM/dd/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        etRegisBirthDay.setText(sdf.format(myCalendar.getTime()));
    }

}
