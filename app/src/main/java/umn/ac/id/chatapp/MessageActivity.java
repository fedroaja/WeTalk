package umn.ac.id.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import umn.ac.id.chatapp.Adapter.MessageAdapter;
import umn.ac.id.chatapp.Fragments.APIService;
import umn.ac.id.chatapp.Model.Chat;
import umn.ac.id.chatapp.Model.User;
import umn.ac.id.chatapp.Notification.Client;
import umn.ac.id.chatapp.Notification.Data;
import umn.ac.id.chatapp.Notification.MyResponse;
import umn.ac.id.chatapp.Notification.Sender;
import umn.ac.id.chatapp.Notification.Token;

public class MessageActivity extends AppCompatActivity {

   /* CircleImageView profile_image;
    TextView username;*/

    FirebaseUser fuser;
    DatabaseReference reference;

    ImageButton btn_send, btn_send_file;
    EditText text_send;

    MessageAdapter messageAdapter;
    List<Chat> mChat;

    RecyclerView recyclerView;

    Intent intent;

    ValueEventListener seenListener;

    String userid;

    APIService apiService;

    boolean notify = false;

    private static final int IMAGE_REQUEST = 22;
    private static final int CAMERA_REQUEST = 33;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS= 7;

    private Uri imageUrl;
    private StorageTask uploadTask;

    String currentPhotoPath;

    StorageReference storageReference;

    String sender_image;
    String receiver_image;
    boolean isimage = true;

    boolean loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);


        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
       /* profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);*/
        btn_send = findViewById(R.id.btn_send);
        text_send = findViewById(R.id.text_send);
        btn_send_file = findViewById(R.id.addFile);

        storageReference = FirebaseStorage.getInstance().getReference("uploads");
        intent = getIntent();
        userid = intent.getStringExtra("userid");
        fuser = FirebaseAuth.getInstance().getCurrentUser();

        sender_image = fuser.getUid();
        receiver_image = userid;


        final CharSequence[] items = {"Camera", "Galery"};
        btn_send_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (haveNetwork()) {
                    checkAndroidVersion();
                    AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity.this);
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            // Do something with the selection
                            if (item == 1) {
                                openImage();
                            } else {
                                openCamera();
                            }

                            dialog.dismiss();
                        }
                    });
                    builder.show();
                }else{
                    Toast.makeText(MessageActivity.this,"Unable send message, Network connection is not available!",Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (haveNetwork()) {
                    notify = true;
                    String msg = text_send.getText().toString();
                    if (!msg.equals("")) {
                        sendMessage(fuser.getUid(), userid, msg, false);
                    } else {
                        Toast.makeText(MessageActivity.this, "You can't send empty message", Toast.LENGTH_SHORT).show();
                    }

                    text_send.setText("");
                }else{
                    Toast.makeText(MessageActivity.this,"Unable send message, Network connection is not available!",Toast.LENGTH_SHORT).show();
                }
            }
        });

        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                getSupportActionBar().setTitle(user.getDisplayname());
                readMessage(fuser.getUid(), userid,user.getImageURL());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        seenMessage(userid);
    }

    //Network Connection cek
    private boolean haveNetwork(){
//        boolean have_WIFI = false;
//        boolean have_MobileData = false;
//
//        ConnectivityManager connectivityManager=(ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
//        NetworkInfo[] networkInfos=connectivityManager.getAllNetworkInfo();
//
//        for (NetworkInfo info:networkInfos){
//            if (info.getTypeName().equalsIgnoreCase("WIFI"))
//                if (info.isConnected())
//                    have_WIFI=true;
//            if (info.getTypeName().equalsIgnoreCase("MOBILE"))
//                if (info.isConnected())
//                    have_MobileData=true;
//        }
//        return have_MobileData||have_WIFI;

        try {
            ConnectivityManager connectivityManager=(ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = null;

            if (connectivityManager != null){
                networkInfo = connectivityManager.getActiveNetworkInfo();
            }
            return networkInfo != null && networkInfo.isConnected();
        }catch (NullPointerException e){
            return false;
        }
    }

    //Image Message Permission
    private void checkAndroidVersion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkAndRequestPermissions();

        } else {
            // code for lollipop and pre-lollipop devices
        }

    }

    private boolean checkAndRequestPermissions() {
        int camera = ContextCompat.checkSelfPermission(MessageActivity.this,
                Manifest.permission.CAMERA);
        int wtite = ContextCompat.checkSelfPermission(MessageActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int read = ContextCompat.checkSelfPermission(MessageActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
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
            ActivityCompat.requestPermissions(MessageActivity.this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
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
                        if (ActivityCompat.shouldShowRequestPermissionRationale(MessageActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(MessageActivity.this, Manifest.permission.CAMERA) || ActivityCompat.shouldShowRequestPermissionRationale(MessageActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
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
                            Toast.makeText(MessageActivity.this, "Go to settings and enable permissions", Toast.LENGTH_LONG)
                                    .show();
                            //                            //proceed with logic by disabling the related features or quit the app.
                        }
                    }
                }
            }
        }

    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MessageActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }

    //Camera
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

    //Open Galery
    private void openImage() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,IMAGE_REQUEST);
    }
    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = MessageActivity.this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }


    //uploading image
    private void uploadImage(){
        final ProgressDialog pd = new ProgressDialog(MessageActivity.this);
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

//                        reference = FirebaseDatabase.getInstance().getReference("Chats");
//                        HashMap<String, Object> map = new HashMap<>();
//                        map.put("imageURL",mUri);
//                        reference.updateChildren(map);

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                        

                        sendMessage(sender_image,receiver_image,mUri,isimage);



                        pd.dismiss();
                    } else{
                        Toast.makeText(MessageActivity.this, "Failed",Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MessageActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });

        }else{
            Toast.makeText(MessageActivity.this,"No image selected", Toast.LENGTH_SHORT).show();
            pd.dismiss();
        }

    }

    //activity result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 20 && resultCode == RESULT_OK){
            userid = data.getStringExtra("userid");
        }

        //Galery
        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null){
            imageUrl = data.getData();

            if (uploadTask != null && uploadTask.isInProgress()){
                Toast.makeText(MessageActivity.this,"Upload in progress",Toast.LENGTH_SHORT).show();
            }else {
                uploadImage();
            }
        }
        //Camera
        else if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK){

            File f = new File(currentPhotoPath);
//            image_profile.setImageURI(Uri.fromFile(f));

            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            imageUrl = Uri.fromFile(f);
            mediaScanIntent.setData(imageUrl);
            this.sendBroadcast(mediaScanIntent);
            if (uploadTask != null && uploadTask.isInProgress()){
                Toast.makeText(MessageActivity.this,"Upload in progress",Toast.LENGTH_SHORT).show();
            }else {
                uploadImage();
            }

        }
    }



    private void seenMessage(final String userid){
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(fuser.getUid()) &&
                    chat.getSender().equals(userid) &&
                    chat.getReceiver() != null &&
                    chat.getSender() != null){
                        HashMap<String,Object> hashmap = new HashMap<>();
                        hashmap.put("isseen", true);
                        snapshot.getRef().updateChildren(hashmap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(final String sender, final String receiver, final String message, final boolean isimage){
        final HashMap<String, Object> hashMap = new HashMap<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

                    hashMap.clear();
                    hashMap.put("sender", sender);
                    hashMap.put("receiver",receiver);
                    hashMap.put("message", message);
                    hashMap.put("isseen", false);
                    hashMap.put("isimage", isimage);
                    hashMap.put("time", ServerValue.TIMESTAMP);
                    hashMap.put("isunsend", false);

                    reference.child("Chats").push().setValue(hashMap);

                    final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                            .child(sender)
                            .child(receiver);

                    final DatabaseReference chatRefRecv = FirebaseDatabase.getInstance().getReference("Chatlist")
                            .child(receiver)
                            .child(sender);

                    final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users")
                            .child(sender);

                    final DatabaseReference userRefRecv = FirebaseDatabase.getInstance().getReference("Users")
                            .child(receiver);

                    //chatlist for reciver
                    chatRefRecv.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            hashMap.clear();
                            if (!dataSnapshot.exists()){
//                              hashMap.put("id",fuser.getUid());
//
//                    chatRef.push().setValue(hashMap2);
                                chatRefRecv.child("id").setValue(sender);
                                chatRefRecv.child("time").setValue(ServerValue.TIMESTAMP);
                            }else {
                                hashMap.put("time",ServerValue.TIMESTAMP);
                                hashMap.put("id",sender);
                                chatRefRecv.updateChildren(hashMap);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    userRefRecv.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            hashMap.clear();
                            hashMap.put("time", ServerValue.TIMESTAMP);
                            userRefRecv.updateChildren(hashMap);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    //chatlist for sender
                    chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            hashMap.clear();
                            if (!dataSnapshot.exists()) {
//                    hashMap.put("id",receiver);
//
//                    chatRef.push().setValue(hashMap1);
                                chatRef.child("id").setValue(receiver);
                                chatRef.child("time").setValue(ServerValue.TIMESTAMP);
                            }else {
                                hashMap.put("time",ServerValue.TIMESTAMP);
                                chatRef.updateChildren(hashMap);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    //update User time
                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            hashMap.clear();
                            hashMap.put("time", ServerValue.TIMESTAMP);
                            userRef.updateChildren(hashMap);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });



                    final String msg = message;

                    reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
                    reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            if (notify) {
                                sendNotification(receiver, user.getUsername(), msg, user.getImageURL());
                            }
                            notify = false;
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

    }

    private void sendNotification(String receiver, final String username, final String message, final String imageURL){
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(fuser.getUid(),R.drawable.logo,imageURL,username+": "+message, "New Message",
                            userid);

                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200){
                                        if (response.body().success != 1){

                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readMessage(final String myid, final String userid, final String imageurl){
        mChat = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mChat.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);

                    if (chat.getReceiver().equals(myid) && chat.getSender().equals(userid) ||
                        chat.getReceiver().equals(userid) && chat.getSender().equals(myid)){
                        mChat.add(chat);
                    }

                    messageAdapter = new MessageAdapter(MessageActivity.this, mChat, imageurl);
                    recyclerView.setAdapter(messageAdapter);



                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void currentUser(String userid){
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("currentuser",userid);
        editor.apply();
    }


    private void status(String status){
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status",status);

        reference.updateChildren(hashMap);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                startActivity(new Intent(MessageActivity.this, ChatActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
        currentUser(userid);
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);
        status("offline");
        currentUser("none");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(MessageActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        finish();

    }
}
