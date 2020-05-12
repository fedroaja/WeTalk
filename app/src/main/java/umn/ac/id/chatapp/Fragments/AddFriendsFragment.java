package umn.ac.id.chatapp.Fragments;

import android.content.Context;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import umn.ac.id.chatapp.ChatActivity;
import umn.ac.id.chatapp.Model.Friendlist;
import umn.ac.id.chatapp.Model.User;
import umn.ac.id.chatapp.R;

import static android.content.Context.CONNECTIVITY_SERVICE;


public class AddFriendsFragment extends Fragment {

    EditText etSearchid;
    ImageButton ibSearchid;
    ImageView ivProfileImage;
    TextView tvDisplayname,tvAlreadyFriend;
    Button btnAddFriend;


    RelativeLayout rlResultid;
    TextInputLayout tilSearch;
    HashMap<String, String> hashMap;

    FirebaseUser fuser;
    DatabaseReference reference;
    private List<Friendlist> mList;

    String IdFriend;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_friends, container, false);

        etSearchid = view.findViewById(R.id.etSearchid);
        ibSearchid = view.findViewById(R.id.btnSearchid);
        rlResultid = view.findViewById(R.id.search_id_result);
        ivProfileImage = view.findViewById(R.id.profile_image);
        tvDisplayname = view.findViewById(R.id.search_displayname);
        tilSearch = view.findViewById(R.id.areaSearch);
        btnAddFriend = view.findViewById(R.id.btn_add_friend);
        tvAlreadyFriend = view.findViewById(R.id.already_friend);

        mList = new ArrayList<>();
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Friendlist").child(fuser.getUid());

        ibSearchid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (haveNetwork()) {
                    //cek if not friends
                    btnAddFriend.setVisibility(View.VISIBLE);
                    tvAlreadyFriend.setVisibility(View.GONE);
                    reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            mList.clear();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Friendlist friendlist = snapshot.getValue(Friendlist.class);
                                mList.add(friendlist);
                            }
                            String txtid = etSearchid.getText().toString();
                            searchUser(txtid);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                }else{
                    Toast.makeText(getActivity(),"Unable search id, Network connection is not available!",Toast.LENGTH_SHORT).show();

                }

            }
        });


        tilSearch.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rlResultid.setVisibility(View.GONE);
                etSearchid.setText("");
            }
        });

        btnAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                final DatabaseReference friendRef = FirebaseDatabase.getInstance().getReference("Friendlist")
                        .child(fuser.getUid())
                        .child(IdFriend);

                //friendlist for sender
                friendRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()){
                            friendRef.child("id").setValue(IdFriend);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                sendTorecv(IdFriend, fuser.getUid());





            }
        });





        return view;
    }

    public void sendTorecv(String idrecv, final String iduser){
        final DatabaseReference friendRefRecv = FirebaseDatabase.getInstance().getReference("Friendlist")
                .child(idrecv)
                .child(iduser);
        //friendlist for reciver
        friendRefRecv.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String,Object> hashMap1 = new HashMap<>();
                if (!dataSnapshot.exists()){
                    friendRefRecv.child("id").setValue(iduser);
                }else{
                    hashMap1.put("id",iduser);
                    friendRefRecv.updateChildren(hashMap1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        btnAddFriend.setVisibility(View.GONE);
        tvAlreadyFriend.setVisibility(View.VISIBLE);
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



    public void searchUser(String s){
        hashMap = new HashMap<>();

        final FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("username").equalTo(s);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                hashMap.clear();
                if (dataSnapshot.getChildrenCount()>0){
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                        User user = snapshot.getValue(User.class);

                        if (!user.getId().equals(fuser.getUid())){
                            IdFriend = user.getId();
                            hashMap.put("userid",user.getId());
                            hashMap.put("displayname",user.getDisplayname());
                            hashMap.put("imageURL",user.getImageURL());
                            tvDisplayname.setText(hashMap.get("displayname"));
                            if (hashMap.get("imageURL").equals("default")){
                                ivProfileImage.setImageResource(R.drawable.default_image);
                            }else {
                                try {
                                    Glide.with(getContext()).load(hashMap.get("imageURL")).into(ivProfileImage);
                                }catch (NullPointerException e){

                                }

                            }

                            rlResultid.setVisibility(View.VISIBLE);
                        }else{
                            rlResultid.setVisibility(View.GONE);
                        }


                    }


                }else{
                    Toast.makeText(getActivity(),"ID not Found!", Toast.LENGTH_SHORT).show();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(getActivity(), (CharSequence) databaseError, Toast.LENGTH_SHORT).show();

            }
        });

    }


}
