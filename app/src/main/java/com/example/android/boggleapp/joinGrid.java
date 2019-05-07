package com.example.android.boggleapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class joinGrid extends Game {


    public String GAME_KEY = "";
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mBoardsDatabaseReference;
    private DatabaseReference hostScoreDatabaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_grid);


        // INITIALIZE THE DATABASE VARIABLES
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mBoardsDatabaseReference = mFirebaseDatabase.getReference().child("Boards");


        // TO GET THE HOST CODE
        Bundle intentBundle = getIntent().getExtras();
        if (intentBundle != null) {
            GAME_KEY = intentBundle.getString("hostCode");
        }


        mBoardsDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                if (dataSnapshot.hasChild(GAME_KEY)) {

                    // EXTRACT THE LETTERS ON GRID
                    // THIS SHOULD BE CHANGED TO SINGLE VALUE EVENT LISTENER

                    String lettersFromGrid = dataSnapshot.child(GAME_KEY).child("Game in session").child("lettersOnGrid").getValue(String.class);


                    // SET UP THE GRID

                    int i = 0;

                    for (int id : buttonIds) {


                        String letterSelected = lettersFromGrid.charAt(i) + "";

                        if (letterSelected.equals("Q")) {
                            letterSelected = "Qu";
                            i++;
                        }

                        //in Boggle Q does not exist alone as a tile

                        ((Button) findViewById(id)).setText(letterSelected);
                        //set the buttons text to the random letter


                        i++;

                    }


                    final String hostName = dataSnapshot.child(GAME_KEY).child("Host").getValue(String.class);

                    // UPDATE SCORE DISPLAYS WITH NAMES
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    // write the Board object to the database
                    TextView joineeScoreDisplay = (TextView) findViewById(R.id.joineeScoreDisplay);
                    joineeScoreDisplay.setText(user.getDisplayName() + "'s score: " + score);
                    TextView hostScoreDisplay = (TextView) findViewById(R.id.hostScoreDisplay);
                    hostScoreDisplay.setText(hostName + "'s Score: " + score);


                    // LISTENING FOR HOST SCORE:
                    hostScoreDatabaseReference = mBoardsDatabaseReference.child(GAME_KEY).child("Game in session").child("scoreHost");
                    hostScoreDatabaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            int hostScore = dataSnapshot.getValue(int.class);
                            // SCORE DISPLAYS
                            TextView hostScoreDisplay = (TextView) findViewById(R.id.hostScoreDisplay);
                            hostScoreDisplay.setText(hostName + "'s Score: " + hostScore);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });


        // WILL SET UP ALL NECESSARY LISTENERS
        setupListeners(GAME_KEY, mBoardsDatabaseReference);


    }


    @Override
    public boolean isValidWord(String attempt) {
        return super.isValidWord(attempt);
    }

    @Override
    public void success() {
        wordsEntered.add(attemptedWord);
        //the word that was entered must be recorded

        int intGain = 0;
        //will store the word's value

        //THE FOLLOWING SWITCH CASE STRUCTURE INCREMENTS THE SCORE ACCORDING TO WORD LENGTH
        // TODO THE FOLLOWING CODE HANDLES DISPLAYING AND TRACKING SCORE

        // intGain will store the number of points the word Entered is worth

        switch (attemptedWord.length()) {

            case 3:
            case 4:
                score++;
                intGain = 1;
                break;
            case 5:
                score += 2;
                intGain = 2;
                break;
            case 6:
                score += 3;
                intGain = 3;
                break;
            case 7:
                score += 4;
                intGain = 4;
                break;
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
                score += 11;
                intGain = 11;
                break;
            default:
                break;
        }


        TextView scoreDisplay = (TextView) findViewById(R.id.hostScoreDisplay);
        scoreDisplay.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName() + "'s score: " + score);
        // updates score display

        TextView historyDisplay = (TextView) findViewById(R.id.historyDisplay);

        //Will update table of words already entered
        String strPast = historyDisplay.getText() + "";


        strUpdate = strPast + "\n" + FirebaseAuth.getInstance().getCurrentUser().getDisplayName() + ":\t" + attemptedWord + " - " + intGain + "\n";


        historyDisplay.setText(strUpdate);

        coordinatesPressed.clear();
        //will clear the history of buttons pressed

        resetGrid();

        mBoardsDatabaseReference.child(GAME_KEY).child("Game in session").child("scoreJoinee").setValue(score);
        mBoardsDatabaseReference.child(GAME_KEY).child("Game in session").child("history").setValue(this.strUpdate);
        // DON"T RESET THE ARRAY YOU NEED TO SET UP A LISTENER
        mBoardsDatabaseReference.child(GAME_KEY).child("Submitted Words").setValue(Arrays.asList(wordsEntered.toArray()));
    }

    @Override
    public void failure() {
        TextView historyDisplay = (TextView) findViewById(R.id.historyDisplay);

        //Will update table of words already entered
        String strPast = historyDisplay.getText() + "";
        strUpdate = strPast + "\n" + FirebaseAuth.getInstance().getCurrentUser().getDisplayName() + ":\tinvalid: " + attemptedWord + "\n";
        if (attemptedWord.length() != 0) historyDisplay.setText(strUpdate);
        //output the message

        resetGrid();
        mFirebaseDatabase.getReference().child("Boards").child(GAME_KEY).child("Game in session").child("history").setValue(this.strUpdate);
    }

    @Override
    public void toEndScreen() {
        Intent intent = new Intent(this, endScreen.class);

        intent.putExtra("FinalScore", score);
        intent.putExtra("Multiplayer?", true);
        intent.putExtra("gameKey", GAME_KEY);


        startActivity(intent);
    }

    @Override
    public void shuffle(View view) {
        super.shuffle(view);
        String updatedLettersOnGrid = "";
        for (int buttonId : buttonIds) updatedLettersOnGrid += ((Button) findViewById(buttonId)).getText();

        mFirebaseDatabase.getReference().child("Boards").child(GAME_KEY).child("Game in session").child("lettersOnGrid").setValue(updatedLettersOnGrid);


    }

}
