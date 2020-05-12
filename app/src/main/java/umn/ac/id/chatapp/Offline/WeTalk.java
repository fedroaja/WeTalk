package umn.ac.id.chatapp.Offline;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class WeTalk extends Application {

    @Override
    public void onCreate() {
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
