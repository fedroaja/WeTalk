package umn.ac.id.chatapp.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.List;

import umn.ac.id.chatapp.MessageActivity;
import umn.ac.id.chatapp.Model.Chat;
import umn.ac.id.chatapp.Model.User;
import umn.ac.id.chatapp.R;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context mContext;
    private List<User> mUsers;
    private boolean ischat;

    String theLastMessage;

    boolean unread;
    boolean isimage;


    public UserAdapter(Context mContext, List<User> mUsers, boolean ischat){
        this.mUsers = mUsers;
        this.mContext = mContext;
        this.ischat = ischat;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.friend_item,parent,false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final User user = mUsers.get(position);
        holder.username.setText(user.getDisplayname());
        if (user.getImageURL().equals("default")){
            holder.profile_image.setImageResource(R.drawable.default_image);
        }else{
            Glide.with(mContext).load(user.getImageURL()).into(holder.profile_image);
        }

        if (ischat){

            lastMessage(user.getId(), holder.last_msg);
            countUnread(user.getId(), holder.FL_unread, holder.num_unread);


            //delete
            final CharSequence[] items = {"Delete", "Cancel"};
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    final TextView textView = new TextView(mContext);
                    textView.setText(user.getDisplayname());
                    textView.setPadding(20, 30, 20, 30);
                    textView.setTextSize(20F);
                    textView.setTextColor(Color.BLACK);
                    AlertDialog.Builder builder = new AlertDialog.Builder((Activity)mContext);
                    builder.setCustomTitle(textView);
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int item) {
                            if (item == 0) {
                                removeChatlist(user.getId());
                            }
                            if (item == 1){
                                dialog.dismiss();
                            }
                            dialog.dismiss();
                        }

                    });
                    builder.show();
                    return false;
                }
            });


        }else {
            holder.last_msg.setVisibility(View.GONE);
        }

        if(ischat){
            if (user.getStatus().equals("online")){
                holder.img_online.setVisibility(View.VISIBLE);
                holder.img_offline.setVisibility(View.GONE);
            }else{
                holder.img_online.setVisibility(View.GONE);
                holder.img_offline.setVisibility(View.VISIBLE);
            }
        }else {
            holder.img_online.setVisibility(View.GONE);
            holder.img_offline.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext,MessageActivity.class);
                intent.putExtra("userid",user.getId());
                mContext.startActivity(intent);
                ((Activity) mContext).finish();
            }
        });

    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView username;
        public ImageView profile_image;
        private ImageView img_online;
        private ImageView img_offline;
        private TextView last_msg, num_unread;
        private FrameLayout FL_unread;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.profile_image);
            img_online = itemView.findViewById(R.id.img_online);
            img_offline = itemView.findViewById(R.id.img_offline);
            last_msg = itemView.findViewById(R.id.last_msg);
            num_unread = itemView.findViewById(R.id.num_unread);
            FL_unread = itemView.findViewById(R.id.FL_unread);
        }
    }

    //cek last message
    private void lastMessage(final String userid, final TextView last_msg){
        theLastMessage = "default";
        isimage = false;
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid) ||
                        chat.getReceiver().equals(userid) && chat.getSender().equals(firebaseUser.getUid())){
                        theLastMessage = chat.getMessage();
                        isimage = chat.isIsimage();
                    }
                }

                switch (theLastMessage){
                    case "default":
                        last_msg.setText("No message");
                        break;
                    default:
                        if (isimage){
                            last_msg.setText("Sent a photo");
                        }else {
                            last_msg.setText(theLastMessage);
                        }
                        break;
                }

                theLastMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //count unread
    private void countUnread(final String userid, final FrameLayout FL_unread, final TextView num_unread){

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int unread = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(firebaseUser.getUid())&& chat.getSender().equals(userid) &&
                    !chat.isIsseen()){
                        unread++;
                    }
                }

                if (unread == 0){
                    FL_unread.setVisibility(View.GONE);
                }else{
                    num_unread.setText(String.valueOf(unread));
                    FL_unread.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //remove Chatlist
    private void removeChatlist(final String userid){

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chatlist");
        reference.child(firebaseUser.getUid()).child(userid).removeValue();

    }

}
