package umn.ac.id.chatapp.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import umn.ac.id.chatapp.ChatActivity;
import umn.ac.id.chatapp.EditActivity;
import umn.ac.id.chatapp.MainActivity;
import umn.ac.id.chatapp.Model.User;
import umn.ac.id.chatapp.R;

import static android.content.Context.CONNECTIVITY_SERVICE;


public class ProfileFragment extends Fragment {

    CircleImageView image_profile;

    TextView username;
    TextView username_id;

    DatabaseReference reference;
    FirebaseUser fuser;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile,container,false);

        image_profile = view.findViewById(R.id.profile_image);
        username = view.findViewById(R.id.username);
        username_id = view.findViewById(R.id.username_id_user);


        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users")
                .child(fuser.getUid());



        final CharSequence[] items = {"Edit Profile", "Log Out"};
        final View imageButton = view.findViewById(R.id.imageButton);
        imageButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(final View view) {
                // do whatever we wish!
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        // Do something with the selection
                        if (item == 1){
                            FirebaseAuth.getInstance().signOut();
                            startActivity(new Intent(view.getContext(), MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                            getActivity().finishAffinity();

                        }else {
                            if (haveNetwork()) {
                                startActivity(new Intent(view.getContext(), EditActivity.class));
                            }else{
                                Toast.makeText(getActivity(),"Unable Edit profile, Network connection is not available!",Toast.LENGTH_SHORT).show();

                            }
                        }

                        dialog.dismiss();
                    }
                });
                builder.show();

            }
        });



        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(isAdded()){
                    User user = dataSnapshot.getValue(User.class);
                    assert user != null;
                    username.setText(user.getDisplayname());
                    username_id.setText("ID : "+user.getUsername());
                    if (user.getImageURL().equals("default")){
                        image_profile.setImageResource(R.drawable.default_image);
                    }else {
                        Glide.with(getActivity().getApplicationContext()).load(user.getImageURL()).into(image_profile);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        return view;
    }

    //Network Connection cek
    private boolean haveNetwork(){
        try {
            ConnectivityManager connectivityManager=(ConnectivityManager) getActivity().getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = null;

            if (connectivityManager != null){
                networkInfo = connectivityManager.getActiveNetworkInfo();
            }
            return networkInfo != null && networkInfo.isConnected();
        }catch (NullPointerException e){
            return false;
        }
    }

}
