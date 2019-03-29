package com.pewds.oussa.meetme;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.EventListener;

public class Principal extends AppCompatActivity {
    ListView listOfNames = null;
    View semi;
    DatabaseReference me;
    private FirebaseListAdapter<StoredUser> Nameadapter;
    private FirebaseListAdapter<StoredUser> ConverAdapter;
    View layoutparent;
    FirebaseAuth mAuth;
    SearchView searchView;
    MenuItem item;
    View nothing;
    View progressBar;
    Query firsQuery;
    ValueEventListener fisEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    startActivity(new Intent(Principal.this, MainActivity.class));
                    finish();
                }
            }
        });
        setContentView(R.layout.activity_principal);
        semi = findViewById(R.id.semi);
        listOfNames = findViewById(R.id.list_of_names);
        layoutparent = findViewById(R.id.list_parent);
        progressBar = findViewById(R.id.progress);
        nothing = findViewById(R.id.nothing);
        if (mAuth.getCurrentUser() != null) {
            Query databaseReference = FirebaseDatabase.getInstance().getReference("Users").orderByChild("userId").equalTo(mAuth.getCurrentUser().getUid());
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            me = ds.getRef();
                            me.keepSynced(true);
                            displayConversations();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

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
        if (item.getItemId() == R.id.signOut) {
            mAuth.signOut();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        item = menu.findItem(R.id.search);
        searchView = (SearchView) item.getActionView();
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
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                layoutparent.setVisibility(View.GONE);
                semi.setVisibility(View.GONE);
                listOfNames.setAdapter(null);
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
        if (firsQuery != null) {
            firsQuery.removeEventListener(fisEvent);
        }
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
                        searchView.clearFocus();
                        item.collapseActionView();
                        layoutparent.setVisibility(View.GONE);
                        semi.setVisibility(View.GONE);
                        Query databaseReference = FirebaseDatabase.getInstance().getReference("Users").orderByChild("userId").equalTo(model.getUserId());
                        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    String key = ds.getKey();
                                    if (!ds.child("conversation").hasChild(mAuth.getCurrentUser().getUid())) {
                                        FirebaseDatabase.getInstance().getReference().child("Users")
                                                .child(key)
                                                .child("conversations")
                                                .child(mAuth.getCurrentUser()
                                                        .getUid()).setValue(new StoredUser(mAuth.getCurrentUser().getDisplayName(), mAuth.getCurrentUser().getUid()));
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                        me.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.child("conversations").hasChild(model.getUserId())) {
                                    me.child("conversations").child(model.getUserId()).setValue(model);
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
        FirebaseListOptions<StoredUser> options = new FirebaseListOptions.Builder<StoredUser>()
                .setLayout(R.layout.conversation)
                .setQuery(me.child("conversations"), StoredUser.class)
                .setLifecycleOwner(this)
                .build();
        ConverAdapter = new FirebaseListAdapter<StoredUser>(options) {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            protected void populateView(View v, final StoredUser model, int position) {
                TextView name = v.findViewById(R.id.conver);
                final TextView last = v.findViewById(R.id.last);
                View parent = v.findViewById(R.id.parent);
                name.setText(model.getUserName());
                final String FirstCase = mAuth.getCurrentUser().getUid() + model.getUserId();
                final String SecoundCase = model.getUserId() + mAuth.getCurrentUser().getUid();
                final Intent[] i = {null};
                firsQuery = FirebaseDatabase.getInstance().getReference().child("hi");
                fisEvent = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(FirstCase)) {
                            last(FirstCase,last);
                            i[0] = new Intent(Principal.this, Chat.class);
                            i[0].putExtra("key", FirstCase);
                        } else if (dataSnapshot.hasChild(SecoundCase)) {
                            last(SecoundCase,last);
                            i[0] = new Intent(Principal.this, Chat.class);
                            i[0].putExtra("key", SecoundCase);
                        } else {
                            last.setText("new");
                            i[0] = new Intent(Principal.this, Chat.class);
                            i[0].putExtra("key", FirstCase);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                };
                firsQuery.addValueEventListener(fisEvent);
                parent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(i[0] != null) {
                            i[0].putExtra("name",model.getUserName());
                            startActivity(i[0]);
                        }
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
                    String message = "";
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
}
