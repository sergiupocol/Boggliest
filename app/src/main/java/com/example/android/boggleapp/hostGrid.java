package com.example.android.boggleapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.Arrays;



public class hostGrid extends Game {


    public String GAME_KEY = "";
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mBoardsDatabaseReference;
    private DatabaseReference joineeScoreDatabaseReference;
    public int joineeScore = 0;
    public String joineeName = "lala";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_grid);


        // INITIALIZE THE DATABASE VARIABLES

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mBoardsDatabaseReference = mFirebaseDatabase.getReference().child("Boards");

        setupGrid();

        String gridLetters = "";

        for (String letter : lettersOnGrid) {
            gridLetters += letter;

        }

        // create a board object to store the game info
        com.example.android.boggleapp.Board board = new Board( gridLetters, "", 0, 0);

        // GETTING THE HOSTCODE FROM THE MAIN ACTIVITY
        Intent intent = getIntent();

        if (intent.getExtras().get("hostCode") != null) {
            GAME_KEY = intent.getExtras().get("hostCode").toString();
        }

        // write the Board object to the database
        mBoardsDatabaseReference.child(GAME_KEY).child("Game in session").setValue(board);

        wordsEntered.add("INVALID");
        // Create a new child to store the submitted words
        mBoardsDatabaseReference.child(GAME_KEY).child("Submitted Words").setValue(Arrays.asList(wordsEntered.toArray()));




        // UPDATE DISPLAYS
        mBoardsDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                if (dataSnapshot.hasChild(GAME_KEY)) {

                    joineeName = dataSnapshot.child(GAME_KEY).child("Joinee").getValue(String.class);


                    // UPDATE SCORE DISPLAYS WITH NAMES
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    // write the Board object to the database
                    // SCORE DISPLAYS
                    TextView joineeScoreDisplay = (TextView) findViewById(R.id.joineeScoreDisplay);
                    TextView hostScoreDisplay = (TextView) findViewById(R.id.hostScoreDisplay);
                    hostScoreDisplay.setText(user.getDisplayName() + "'s score: " + score);
                    joineeScoreDisplay.setText(joineeName + "'s Score: " + score);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });



        joineeScoreDatabaseReference = mBoardsDatabaseReference.child(GAME_KEY).child("Game in session").child("scoreJoinee");
        joineeScoreDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                joineeScore = dataSnapshot.getValue(int.class);
                // SCORE DISPLAYS
                TextView joineeScoreDisplay = (TextView) findViewById(R.id.joineeScoreDisplay);
                joineeScoreDisplay.setText(joineeName + "'s Score: " + joineeScore);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });




    }


    @Override
    public boolean isValidWord(String attempt) {
        // RIGHT HERE SERGIU MODIFYwordsEnterred to reflect player's input
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


        strUpdate = strPast + "\n" + FirebaseAuth.getInstance().getCurrentUser().getDisplayName() + ":\t" +attemptedWord + " - " + intGain + "\n";


        historyDisplay.setText(strUpdate);

        coordinatesPressed.clear();
        //will clear the history of buttons pressed

        resetGrid();

        mBoardsDatabaseReference.child(GAME_KEY).child("Game in session").child("scoreHost").setValue(score);
        mBoardsDatabaseReference.child(GAME_KEY).child("Game in session").child("history").setValue(this.strUpdate);
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
