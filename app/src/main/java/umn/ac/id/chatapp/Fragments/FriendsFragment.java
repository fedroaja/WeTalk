package umn.ac.id.chatapp.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

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

import umn.ac.id.chatapp.Adapter.UserAdapter;
import umn.ac.id.chatapp.ChatActivity;
import umn.ac.id.chatapp.Model.Friendlist;
import umn.ac.id.chatapp.Model.User;
import umn.ac.id.chatapp.R;

public class FriendsFragment extends Fragment {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> mUser;
    private List<Friendlist> mFriend;
    FirebaseUser fuser;
    private TextView myfriends;
    private String currentUserId;
    DatabaseReference reference, FriendsRef;
    private FirebaseAuth mAuth;
    private int countFriends = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view  = inflater.inflate(R.layout.fragment_friends,container,false);
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        myfriends = (TextView) view.findViewById(R.id.my_friends_button);
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        mFriend = new ArrayList<>();

//        mUser = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Friendlist").child(fuser.getUid());
        FriendsRef = FirebaseDatabase.getInstance().getReference("Friendlist");
        FriendsRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    countFriends = (int) dataSnapshot.getChildrenCount();
                    myfriends.setText(Integer.toString(countFriends )+ " Friends");
                }else
                {
                    myfriends.setText("0 friends");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mFriend.clear();
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                    Friendlist friendlist = dataSnapshot1.getValue(Friendlist.class);
                    mFriend.add(friendlist);
                }
                readUsers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


//        readUsers();

        return view;
    }

    private void readUsers(){
        mUser = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");


//        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
//
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUser.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);
                    for (Friendlist friendlist : mFriend){
                        if (user.getId()!=null && user.getId().equals(friendlist.getId())){
                            mUser.add(user);
                        }
                    }
                }
                userAdapter = new UserAdapter(getContext(), mUser, false);
                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
