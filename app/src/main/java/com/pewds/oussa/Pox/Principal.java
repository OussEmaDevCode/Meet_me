package com.pewds.oussa.Pox;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;
import io.fabric.sdk.android.Fabric;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.pewds.oussa.Pox.models.StoredUser;
import com.pewds.oussa.Pox.models.conversation;
import com.squareup.picasso.Picasso;

public class Principal extends AppCompatActivity {
    ListView listOfNames = null;
    View semi;
    DatabaseReference me;
    private FirebaseListAdapter<StoredUser> Nameadapter;
    private FirebaseListAdapter<conversation> ConverAdapter;
    View layoutparent;
    FirebaseAuth mAuth;
    SearchView searchView;
    MenuItem item;
    View nothing;
    View progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_principal);
        semi = findViewById(R.id.semi);
        listOfNames = findViewById(R.id.list_of_names);
        layoutparent = findViewById(R.id.list_parent);
        progressBar = findViewById(R.id.progress);
        nothing = findViewById(R.id.nothing);
        if (mAuth.getCurrentUser() != null) {
            me = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
            me.keepSynced(true);
            displayConversations();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!isOnline()){
                    Snackbar.make(findViewById(android.R.id.content),"Please check your internet connection",Snackbar.LENGTH_SHORT).show();
                }
            }
        },1000);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settings) {
            startActivity(new Intent(Principal.this,Settings.class));
        }else if(item.getItemId() == R.id.share){
            startActivity(new Intent(Principal.this,Send.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        item = menu.findItem(R.id.search);
        searchView = (SearchView) item.getActionView();
        searchView.setQueryHint("look for friends");
        searchView.onActionViewExpanded();
        searchView.setSubmitButtonEnabled(false);
        searchView.setQueryRefinementEnabled(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.equals("") && !newText.equals(" ") && newText.length() > 0 && isOnline()) {
                    displayNames(newText);
                }
                return true;
            }
        });
        item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                semi.setVisibility(View.VISIBLE);
                searchView.requestFocus();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                layoutparent.setVisibility(View.GONE);
                semi.setVisibility(View.GONE);
                listOfNames.setAdapter(null);
                hideKeyboard(Principal.this);
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void displayNames(String queryText) {
        FirebaseListOptions<StoredUser> options = new FirebaseListOptions.Builder<StoredUser>()
                .setLayout(R.layout.name)
                .setQuery(FirebaseDatabase.getInstance().getReference().child("Users").orderByChild("userName")
                        .startAt(queryText)
                        .endAt(queryText + "\uf8ff").limitToFirst(6), StoredUser.class)
                .setLifecycleOwner(this)
                .build();
        Nameadapter = new FirebaseListAdapter<StoredUser>(options) {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            protected void populateView(View v, final StoredUser model, int position) {
                TextView name = v.findViewById(R.id.name);
                name.setText(model.getUserName());
                name.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String creation = mAuth.getCurrentUser().getUid()+","+model.getUserId();
                        me.child("conversations").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                //-----------------------me------------------------------------
                                if (!dataSnapshot.hasChild(model.getUserId())) {

                                    me.child("conversations")
                                            .child(model.getUserId())
                                            .setValue(new conversation(creation));
                                    //---------------------------him----------------------------
                                        FirebaseDatabase.getInstance().getReference().child("Users")
                                                .child(model.getUserId())
                                                .child("conversations")
                                                .child(mAuth.getCurrentUser().getUid())
                                                .setValue(new conversation(creation));
                                    Toast.makeText(getApplicationContext(),"You're now friend with "+model.getUserName(),Toast.LENGTH_LONG).show();

                                }else {
                                    Toast.makeText(getApplicationContext(),"You're already friend with "+model.getUserName(),Toast.LENGTH_LONG).show();

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                });
            }
        };
        listOfNames.setAdapter(Nameadapter);
        layoutparent.setVisibility(View.VISIBLE);
    }

    private void displayConversations() {
        final ListView conversations = findViewById(R.id.list_of_conversations);
        FirebaseListOptions<conversation> options = new FirebaseListOptions.Builder<conversation>()
                .setLayout(R.layout.conversation)
                .setQuery(me.child("conversations"), conversation.class)
                .setLifecycleOwner(this)
                .build();
        ConverAdapter = new FirebaseListAdapter<conversation>(options) {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            protected void populateView(View v, final conversation model, int position) {
                final TextView name = v.findViewById(R.id.conver);
                final CircleImageView profile = v.findViewById(R.id.profile);
                final TextView last = v.findViewById(R.id.last);
                final View parent = v.findViewById(R.id.parent);
                profile.setImageResource(R.drawable.ic_person_black_24dp);
                String uId;
                String[] id = model.getConversationId().split(",");
                uId = id[0].equals(mAuth.getCurrentUser().getUid())? id[1]:id[0];
                FirebaseDatabase.getInstance().getReference()
                        .child("Users")
                        .child(uId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue() != null){
                            String photoUri = dataSnapshot.child("photoUri").getValue().toString();
                            final String UserName = dataSnapshot.child("userName").getValue().toString();
                            name.setText(UserName);
                            if(photoUri!= null && !photoUri.equals("")) {
                                Picasso.get()
                                        .load(photoUri)
                                        .resize(58, 58)
                                        .centerCrop()
                                        .into(profile);
                            }else {
                                profile.setImageResource(R.drawable.ic_person_black_24dp);
                            }
                            last.setText("new !");
                            last(model.getConversationId(),last);
                            parent.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    startActivity(new Intent(Principal.this,Chat.class).putExtra("key",model.getConversationId()).putExtra("name",UserName));
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        };
        me.child("conversations").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    progressBar.setVisibility(View.GONE);
                    nothing.setVisibility(View.VISIBLE);
                } else {
                    conversations.setEmptyView(progressBar);
                    nothing.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        conversations.setAdapter(ConverAdapter);
    }
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
    public void last(String conv ,final TextView last){
        Query lastQuery = FirebaseDatabase.getInstance().getReference().child("hi").child(conv).orderByKey().limitToLast(1);
        lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    if(ds.child("messageUserId").getValue().toString().equals(mAuth.getCurrentUser().getUid())) {
                        last.setText("You: "+ds.child("messageText").getValue().toString());
                    }else {
                        last.setText(ds.child("messageText").getValue().toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
