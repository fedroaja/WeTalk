package umn.ac.id.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import umn.ac.id.chatapp.Model.User;

public class EditActivity extends AppCompatActivity {

    CircleImageView image_profile;
    FrameLayout change_profile;
    TextView tvId,tvDisplayname,tvBirthday;
    LinearLayout llDisplayName, llBirthday, llPassword;
    Calendar myCalendar;

    DatabaseReference reference;
    FirebaseUser fuser;

    StorageReference storageReference;
    private static final int IMAGE_REQUEST = 11;
    private static final int CAMERA_REQUEST = 23;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS= 7;
    private Uri imageUrl;
    private StorageTask uploadTask;

    String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        myCalendar = Calendar.getInstance();

        image_profile = (CircleImageView) findViewById(R.id.profile_image);
        tvId = (TextView) findViewById(R.id.id_value_user);
        tvDisplayname = (TextView) findViewById(R.id.displayname_value_user);
        tvBirthday = (TextView) findViewById(R.id.birthday_value_user);
        change_profile = (FrameLayout) findViewById(R.id.change_profile);

        llDisplayName = (LinearLayout) findViewById(R.id.area_displayname);
        llBirthday = (LinearLayout) findViewById(R.id.area_birthday);
        llPassword = (LinearLayout) findViewById(R.id.area_password);

        storageReference = FirebaseStorage.getInstance().getReference("uploads");

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users")
                .child(fuser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                tvId.setText(user.getUsername());
                tvDisplayname.setText(user.getDisplayname());
                tvBirthday.setText(user.getBirthday());
                if (user.getImageURL().equals("default")){
                    image_profile.setImageResource(R.drawable.default_image);
                }else {
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(image_profile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //change profile picture
        final CharSequence[] items = {"Camera", "Select Image"};
        change_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndroidVersion();
                AlertDialog.Builder builder = new AlertDialog.Builder(EditActivity.this);
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        // Do something with the selection
                        if (item == 1){
                            openImage();
                        }else {
                            openCamera();
                        }

                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });
        //change display name
        llDisplayName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(EditActivity.this);
                final EditText edittext = new EditText(EditActivity.this);
                alert.setMessage("New Display Name");
                edittext.setBackgroundResource(R.drawable.bg_edittext);

                alert.setView(edittext);

                alert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String newName = edittext.getText().toString();
                        if (TextUtils.isEmpty(newName)){
                            Toast.makeText(EditActivity.this,"All fields are required",Toast.LENGTH_SHORT).show();
                        }else{
                            reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("displayname",newName);

                            reference.updateChildren(map);
                        }


                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // what ever you want to do with No option.
                    }
                });

                alert.show();
            }
        });

        //change Birthday

        llBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EditActivity.this);
                final DatePicker picker = new DatePicker(EditActivity.this);
                picker.setCalendarViewShown(false);

                builder.setTitle("New BirthDay");
                builder.setView(picker);
                builder.setNegativeButton("Cancel", null);
                builder.setPositiveButton("Confrim", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //do something
                        myCalendar.set(Calendar.YEAR, picker.getYear());
                        myCalendar.set(Calendar.MONTH, picker.getMonth());
                        myCalendar.set(Calendar.DAY_OF_MONTH, picker.getDayOfMonth());


                        String myFormat = "MM/dd/yy"; //In which you need put here
                        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                        String newBirthday = sdf.format(myCalendar.getTime());

                        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("birthday",newBirthday);

                        reference.updateChildren(map);

                    }
                });

                builder.show();
            }
        });

        //change Password
        llPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(EditActivity.this);

                final TextView oldpastitle = new TextView(EditActivity.this);
                oldpastitle.setText("Old Password");
                oldpastitle.setTextSize(20);
                oldpastitle.setTextColor(Color.BLACK);
                final EditText oldpas = new EditText(EditActivity.this);
                oldpas.setBackgroundResource(R.drawable.bg_edittext);
                oldpas.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

                final TextView newpastitle = new TextView(EditActivity.this);
                newpastitle.setText("New Password");
                newpastitle.setTextSize(20);
                newpastitle.setTextColor(Color.BLACK);
                final EditText newpas = new EditText(EditActivity.this);
                newpas.setBackgroundResource(R.drawable.bg_edittext);
                newpas.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);



                LinearLayout lay = new LinearLayout(EditActivity.this);
                lay.setOrientation(LinearLayout.VERTICAL);
                lay.setDividerPadding(8);
                lay.addView(oldpastitle);
                lay.addView(oldpas);
                lay.addView(newpastitle);
                lay.addView(newpas);
                alert.setView(lay);

                alert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        final ProgressDialog pd = new ProgressDialog(EditActivity.this);
                        pd.setMessage("Loading...");
                        pd.show();

                        final String oldpass = oldpas.getText().toString();
                        final String newpass = newpas.getText().toString();

                        if (TextUtils.isEmpty(oldpass) || TextUtils.isEmpty(newpass)){
                            Toast.makeText(EditActivity.this,"All fields are required", Toast.LENGTH_SHORT).show();
                            pd.dismiss();
                        }else if (newpass.length() < 6){
                            Toast.makeText(EditActivity.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                            pd.dismiss();
                        }else{
                            final String email = fuser.getEmail();
                            AuthCredential credential = EmailAuthProvider.getCredential(email,oldpass);

                            fuser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        fuser.updatePassword(newpass).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(!task.isSuccessful()){
                                                    Toast.makeText(EditActivity.this,"Something went wrong. Please try again later",Toast.LENGTH_SHORT).show();
                                                    pd.dismiss();
                                                }else {
                                                    Toast.makeText(EditActivity.this,"Password Successfully Modified",Toast.LENGTH_SHORT).show();
                                                    pd.dismiss();
                                                }
                                            }
                                        });
                                    }else {
                                        Toast.makeText(EditActivity.this,"Authentication Failed",Toast.LENGTH_SHORT).show();
                                        pd.dismiss();
                                    }
                                }
                            });
                        }


                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // what ever you want to do with No option.
                    }
                });

                alert.show();
            }
        });

    }



    //Request permission

    private void checkAndroidVersion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkAndRequestPermissions();

        } else {
            // code for lollipop and pre-lollipop devices
        }

    }

    private boolean checkAndRequestPermissions() {
        int camera = ContextCompat.checkSelfPermission(EditActivity.this,
                Manifest.permission.CAMERA);
        int wtite = ContextCompat.checkSelfPermission(EditActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int read = ContextCompat.checkSelfPermission(EditActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (wtite != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (camera != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (read != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(EditActivity.this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }


    private void openCamera(){
        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (camera.resolveActivity(getPackageManager()) != null)
        {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            }catch (IOException e){

            }
            if (photoFile != null){
                Uri photoURI = FileProvider.getUriForFile(this,
                        "umn.ac.id.android.fileprovider",
                        photoFile);
                camera.putExtra(MediaStore.EXTRA_OUTPUT,photoURI);
                startActivityForResult(camera,CAMERA_REQUEST);
            }
        }

    }

    //Create camera image path
    private File createImageFile() throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
//        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void openImage() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,IMAGE_REQUEST);
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = EditActivity.this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    //uploading image
     private void uploadImage(){
        final ProgressDialog pd = new ProgressDialog(EditActivity.this);
        pd.setMessage("Uploading");
        pd.show();

        if (imageUrl != null){
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis()
            +":"+getFileExtension(imageUrl));

            uploadTask = fileReference.putFile(imageUrl);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {

                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()){
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        assert downloadUri != null;
                        String mUri = downloadUri.toString();

                        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("imageURL",mUri);
                        reference.updateChildren(map);

                        pd.dismiss();
                    } else{
                        Toast.makeText(EditActivity.this, "Failed",Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(EditActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });

        }else{
            Toast.makeText(EditActivity.this,"No image selected", Toast.LENGTH_SHORT).show();
            pd.dismiss();
        }

     }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Galery
        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK
          && data != null && data.getData() != null){
            imageUrl = data.getData();

            if (uploadTask != null && uploadTask.isInProgress()){
                Toast.makeText(EditActivity.this,"Upload in progress",Toast.LENGTH_SHORT).show();
            }else {
                uploadImage();
            }
        }
        //Camera
        else if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK){
            File f = new File(currentPhotoPath);
            image_profile.setImageURI(Uri.fromFile(f));

            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            imageUrl = Uri.fromFile(f);
            mediaScanIntent.setData(imageUrl);
            this.sendBroadcast(mediaScanIntent);
            if (uploadTask != null && uploadTask.isInProgress()){
                Toast.makeText(EditActivity.this,"Upload in progress",Toast.LENGTH_SHORT).show();
            }else {
                uploadImage();
            }

        }
    }

    //request permission
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Log.d("in fragment on request", "Permission callback called-------");
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {

                Map<String, Integer> perms = new HashMap<>();
                // Initialize the map with both permissions
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for both permissions
                    if (perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        Log.d("in fragment on request", "CAMERA & WRITE_EXTERNAL_STORAGE READ_EXTERNAL_STORAGE permission granted");
                        // process the normal flow
                        //else any one or both the permissions are not granted
                    } else {
                        Log.d("in fragment on request", "Some permissions are not granted ask again ");
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
//                        // shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(EditActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(EditActivity.this, Manifest.permission.CAMERA) || ActivityCompat.shouldShowRequestPermissionRationale(EditActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            showDialogOK("Camera and Storage Permission required for this app",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    checkAndRequestPermissions();
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    // proceed with logic by disabling the related features or quit the app.
                                                    break;
                                            }
                                        }
                                    });
                        }
                        //permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                        else {
                            Toast.makeText(EditActivity.this, "Go to settings and enable permissions", Toast.LENGTH_LONG)
                                    .show();
                            //                            //proceed with logic by disabling the related features or quit the app.
                        }
                    }
                }
            }
        }

    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(EditActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }


}
