package umn.ac.id.chatapp.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.jsibbold.zoomage.ZoomageView;

import java.util.HashMap;
import java.util.List;

import umn.ac.id.chatapp.FullPictureActivity;
import umn.ac.id.chatapp.MessageActivity;
import umn.ac.id.chatapp.Model.Chat;
import umn.ac.id.chatapp.Model.User;
import umn.ac.id.chatapp.R;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    public static final int MSG_TYPE_UNSEND = 2;
    private Context mContext;
    private List<Chat> mChat;
    private String imageurl;
    private boolean isunsend;

    FirebaseUser fuser;
    String msgPosition;
    String msgUnsend = "";

    public MessageAdapter(Context mContext, List<Chat> mChat, String imageurl){
        this.mChat = mChat;
        this.mContext = mContext;
        this.imageurl = imageurl;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        msgUnsend="";
        if (viewType == MSG_TYPE_RIGHT) {

                msgPosition = "sender";
//                View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
//                return new MessageAdapter.ViewHolder(view);
                View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right_image, parent, false);
                return new MessageAdapter.ViewHolder(view);
        }
        else if (viewType == MSG_TYPE_LEFT){

                msgPosition = "recv";
//                View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
//                return new MessageAdapter.ViewHolder(view);

                View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left_image, parent, false);
                return new MessageAdapter.ViewHolder(view);
        }
        else{
            msgUnsend = "unsend";
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_unsend, parent, false);
            return new MessageAdapter.ViewHolder(view);
        }


    }

    @Override
    public void onBindViewHolder(@NonNull final MessageAdapter.ViewHolder holder, final int position) {
        holder.setIsRecyclable(false);
        final Chat chat = mChat.get(position);

        if (!msgUnsend.equals("unsend")) {
            if (!mChat.get(position).isIsimage()) {
                if (msgPosition.equals("sender")) {
                    holder.show_message_image.setVisibility(View.GONE);
                    holder.txt_seen_image.setVisibility(View.GONE);
                    holder.show_message.setVisibility(View.VISIBLE);
                    holder.txt_seen.setVisibility(View.VISIBLE);
                    holder.show_message.setText(chat.getMessage());
                } else if (msgPosition.equals("recv")) {
                    holder.show_message_image_2.setVisibility(View.GONE);
                    holder.show_message_2.setVisibility(View.VISIBLE);
                    holder.show_message_2.setText(chat.getMessage());
                }

            } else {
                if (msgPosition.equals("sender")) {
                    holder.show_message_image.setVisibility(View.VISIBLE);
                    holder.txt_seen_image.setVisibility(View.VISIBLE);
                    holder.show_message.setVisibility(View.GONE);
                    holder.txt_seen.setVisibility(View.GONE);
                    Glide.with(mContext.getApplicationContext()).load(mChat.get(position).getMessage()).into(holder.show_message_image);


                } else if (msgPosition.equals("recv")) {
                    holder.show_message_image_2.setVisibility(View.VISIBLE);
                    holder.show_message_2.setVisibility(View.GONE);
                    Glide.with(mContext.getApplicationContext()).load(mChat.get(position).getMessage()).into(holder.show_message_image_2);
                }
            }
        }else{

        }

        //unsend
        if(!msgUnsend.equals("unsend")) {
            if (msgPosition.equals("sender")) {
                final CharSequence[] items = {"Unsend", "Cancel"};
                holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder((Activity) mContext);
                        builder.setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int item) {
                                if (item == 0) {
                                    Long time = mChat.get(position).getTime();
                                    unsendMessage(chat.getSender(), chat.getReceiver(), time);
                                }
                                if (item == 1) {
                                    dialog.dismiss();
                                }
                                dialog.dismiss();
                            }

                        });
                        builder.show();
                        return false;
                    }
                });

            }
        }

       //profiile
        if (!msgUnsend.equals("unsend")) {
            if (imageurl.equals("default")) {
                holder.profile_image.setImageResource(R.drawable.default_image);
            } else {
                Glide.with(mContext).load(imageurl).into(holder.profile_image);
            }
        }

        //position == mChat.size()-1
        if (!msgUnsend.equals("unsend")) {
            if (chat.isIsseen()) {
                if (chat.isIsimage()) {
                    if (msgPosition.equals("sender")) {
                        holder.txt_seen_image.setText("Read");
                    }
                } else {
                    if (msgPosition.equals("sender")) {
                        holder.txt_seen.setText("Read");
                    }
                }
            } else {
                if (chat.isIsimage()) {
                    if (msgPosition.equals("sender")) {
                        holder.txt_seen_image.setText("Delivered");
                    }
                } else {
                    if (msgPosition.equals("sender")) {
                        holder.txt_seen.setText("Delivered");
                    }
                }
            }
        }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!msgUnsend.equals("unsend")) {
                        if (mChat.get(position).isIsimage()) {
                            Intent fullPicture = new Intent(mContext, FullPictureActivity.class);
                            fullPicture.putExtra("source", mChat.get(position).getMessage());
                            fullPicture.putExtra("userid", chat.getSender());
                            mContext.startActivity(fullPicture);
                        }
                    }
                }
            });


    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView show_message,show_message_2;
        public ImageView show_message_image, show_message_image_2;
        public TextView txt_seen_image;
        public ImageView profile_image;
        public TextView txt_seen;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txt_seen_image = itemView.findViewById(R.id.txt_seen_image);
            show_message_image = itemView.findViewById(R.id.show_message_image);
            show_message = itemView.findViewById(R.id.show_message);
            show_message_image_2 = itemView.findViewById(R.id.show_message_image_2);
            show_message_2 = itemView.findViewById(R.id.show_message_2);
            profile_image = itemView.findViewById(R.id.profile_image);
            txt_seen = itemView.findViewById(R.id.txt_seen);
        }


    }

    @Override
    public int getItemViewType(int position) {
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        if (mChat.get(position).isIsunsend()){
            return MSG_TYPE_UNSEND;
        }
        if (mChat.get(position).getSender().equals(fuser.getUid())){
            return MSG_TYPE_RIGHT;
        }else{
            return MSG_TYPE_LEFT;
        }
    }

    //unsend message
    private void unsendMessage(final String sender, final String recvier , final Long time){
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(recvier) &&
                            chat.getSender().equals(sender) &&
                            chat.getTime().equals(time)){
                        HashMap<String,Object> hashmap = new HashMap<>();
                        hashmap.put("isunsend", true);
                        snapshot.getRef().updateChildren(hashmap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
