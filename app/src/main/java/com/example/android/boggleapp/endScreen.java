package com.example.android.boggleapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;

public class endScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_screen);

        Bundle intentBundle = getIntent().getExtras();

        boolean isMultiplayer = false;
        String gameKey = "";
        String wordHistory = "";

        if (intentBundle != null) {
            final int finalScore = intentBundle.getInt("FinalScore");
            isMultiplayer = intentBundle.getBoolean("Multiplayer?");
            gameKey = intentBundle.getString("gameKey");
            wordHistory = intentBundle.getString("wordHistory");




        if (!isMultiplayer) {
            TextView finalScoreDisplay = (TextView) findViewById(R.id.finalScoreDisplay);

            finalScoreDisplay.setText("Final Score: " + finalScore);


            TextView goMessage = (TextView) findViewById(R.id.game_over_message);


            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if (user != null) {
                goMessage.setText("Game over " + user.getDisplayName() +"!");
            }
        } else {

            // MULTIPLAYER END SCREEN

            Log.w("SERGIU LOOK HEREEEE", gameKey + "");
            DatabaseReference sessionRef = FirebaseDatabase.getInstance().getReference().child("Boards").child(gameKey + "");

            // HERE CHANGE THE VALUE OF gameOver
            FirebaseDatabase.getInstance().getReference().child("Boards").child(gameKey + "").child("inSession").child("gameOver").setValue(true);
            // NOW UR CLOUD FUNCTION SHOULD TAKE OVER AND EMAIL THE WORD HISTORY


            sessionRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int scoreHost = dataSnapshot.child("Game in session").child("scoreHost").getValue(int.class);
                    int scoreJoinee = dataSnapshot.child("Game in session").child("scoreJoinee").getValue(int.class);
                    String hostName = (String) dataSnapshot.child("Host").getValue();
                    String joineeName = (String) dataSnapshot.child("Joinee").getValue();

                    TextView finalScoreDisplay = (TextView) findViewById(R.id.finalScoreDisplay);
                    finalScoreDisplay.setText("Final Score: " + finalScore);
                    TextView goMessage = (TextView) findViewById(R.id.game_over_message);
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        goMessage.setText("Game over " + user.getDisplayName() + "!");
                    }


                    // DISPLAY WINNER
                    TextView winnerDisplay = (TextView) findViewById(R.id.finalScoreDisplay);
                    if (scoreHost == scoreJoinee) {
                        winnerDisplay.setText("TIE!!!");
                    } else if (scoreHost > scoreJoinee) {
                        winnerDisplay.setText(" " + hostName + " won!!!");
                    } else {
                        winnerDisplay.setText(" " + joineeName + " won!!!");
                    }


                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }


        }

    }


    /**
     * TODO: send the user back to the home screen
     *
     * @param view the menu button
     */
    public void goToMenu(View view) {

        Intent intent = new Intent(this, MainActivity.class);

        startActivity(intent);

    }
}
