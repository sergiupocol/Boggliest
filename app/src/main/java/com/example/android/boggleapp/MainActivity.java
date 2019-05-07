package com.example.android.boggleapp;

import android.app.Dialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity {


    // Firebase Instance Variables:
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private FirebaseDatabase mFirebaseDatabase;
    // will reference specific part of database
    private DatabaseReference mBoardsDatabaseReference;
    private Dialog waitingRoom;
    private int hostCode = 0;
    public static final int RC_SIGN_IN = 1;

    private DatabaseReference inSessionReference;
    private ChildEventListener inSessionListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // INITIALIZE THE DATABASE VARIABLES

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mBoardsDatabaseReference = mFirebaseDatabase.getReference().child("Boards");


        // initialize the FirebaseAuth instance

        mFirebaseAuth = FirebaseAuth.getInstance();
        // DO SIGN IN STUFF


        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {

                    // user is signed innnn
                    Toast.makeText(MainActivity.this, "Welcome " + user.getDisplayName() + "!", Toast.LENGTH_SHORT).show();

                } else {

                    // user is not signed in!!!


                    // Choose authentication providers
                    List<AuthUI.IdpConfig> providers = Arrays.asList(
                            new AuthUI.IdpConfig.EmailBuilder().build(),
                            new AuthUI.IdpConfig.GoogleBuilder().build());

                    // Create and launch sign-in intent
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(providers)
                                    .build(),
                            RC_SIGN_IN);
                }

            }
        };


        // FOR THE WAITING ROOM
        waitingRoom = new Dialog(this);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "sign in cancelled!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }


    /**
     * SINGLE PLAYER
     *
     * @param view single player button
     */
    public void play(View view) {
        Intent intent = new Intent(this, Game.class);
        startActivity(intent);
    }


    /**
     * will initiate the joining process
     *
     * @param view the submit button
     */
    public void attemptJoin(View view) {

        final EditText codeInput = (EditText) waitingRoom.findViewById(R.id.codeInput);
        final String codeAttempt = codeInput.getText().toString();
        final Intent intent = new Intent(this, joinGrid.class);
        intent.putExtra("hostCode", codeAttempt);


        Log.w("LOOK HERE", codeAttempt);
        final Intent endIntent = new Intent(this, endScreen.class);


        mBoardsDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(codeAttempt)) {
                    codeInput.setText("SUCCESS!!");


                    mBoardsDatabaseReference.child(codeAttempt + "").child("inSession").child("inProgress").setValue(true);

                    // START THE NEXT ACTIVITY SISSS
                    startActivity(intent);


                    // UPDATE SCORE DISPLAYS WITH NAMES
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    // write the Board object to the database
                    mBoardsDatabaseReference.child(codeAttempt + "").child("Joinee").setValue(user.getDisplayName());




                    // ATTACH A LISTENER
                    inSessionReference = mBoardsDatabaseReference.child(codeAttempt + "");
                    inSessionListener = new ChildEventListener() {


                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                            boolean inSession = dataSnapshot.getValue(Boolean.class);
                            if (inSession) {

                                Toast.makeText(MainActivity.this, "the game has started", Toast.LENGTH_SHORT).show();
                            } else {



                                endIntent.putExtra("FinalScore", -2345);

                                startActivity(endIntent);
                            }

                        }
                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                        }
                        @Override
                        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    };

                    //
                    inSessionReference.child("inSession").addChildEventListener(inSessionListener);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * Allows user to join a game and displays a dialogue
     *
     * @param view as the joinGame button
     */
    public void joinGame(View view) {
        waitingRoom.setContentView(R.layout.joining_room);
        waitingRoom.show();

    }


    /**
     * kickoff the multiplayer process
     *
     * @param view the multiplayer button :D
     */
    public void multiplay(View view) {

        hostCode = (int) (Math.random() * 9000 + 1000);


        Log.w("lol", "" + hostCode);


        waitingRoom.setContentView(R.layout.waiting_room);


        TextView userDisplay = (TextView) waitingRoom.findViewById(R.id.userDisplay);
        TextView codeDisplay = (TextView) waitingRoom.findViewById(R.id.codeDisplay);

        userDisplay.setText(mFirebaseAuth.getCurrentUser().getDisplayName() + " vs.");
        codeDisplay.setText("Host code: " + hostCode + "");

        waitingRoom.show();


        // ISSUE I WANT THE BOARD TO ONLY DISPLAY IF THE OTHER PLAYER HAS JOINED
        //Add a listener that only moves on when the second player joins and starts the game


        final Intent intent = new Intent(this, hostGrid.class);
        final Intent endIntent = new Intent(this, endScreen.class);

        intent.putExtra("hostCode", hostCode);

        inSessionReference = mBoardsDatabaseReference.child(hostCode + "");
        inSessionReference.child("inSession").child("inProgress").setValue(false);
        inSessionListener = new ChildEventListener() {


            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                boolean inSession = dataSnapshot.getValue(Boolean.class);
                if (inSession) {
                    // UPDATE SCORE DISPLAYS WITH NAMES
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    // write the Board object to the database
                    inSessionReference.child("Host").setValue(user.getDisplayName());

                    Toast.makeText(MainActivity.this, "the game has started", Toast.LENGTH_SHORT).show();

                    startActivity(intent);
                } else {

                    endIntent.putExtra("FinalScore", -2345);
                    endIntent.putExtra("Multiplayer?", false);
                    endIntent.putExtra("gameKey", 0);

                    startActivity(endIntent);
                }

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };


        inSessionReference.child("inSession").addChildEventListener(inSessionListener);


    }

    @Override
    protected void onResume() {
        super.onResume();

        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();

        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);

    }


    public void signout(View view) {

        AuthUI.getInstance().signOut(this);
    }
}
