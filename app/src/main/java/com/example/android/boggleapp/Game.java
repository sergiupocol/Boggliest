package com.example.android.boggleapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.graphics.Color.DKGRAY;
import static android.graphics.Color.WHITE;

public class Game extends AppCompatActivity {
    final int GAME_DURATION = 20000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        setupGrid();



        /*
         * The following code will store acceptable english words
         * in a string called strBoggleWords
         */



        // from https://www.youtube.com/watch?v=UIIpCt2S5Ls
        StringBuffer sbuffer = new StringBuffer();
        InputStream is = this.getResources().openRawResource(R.raw.dict);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        //locates the file dict.txt and opens it for reading



        if (is != null) {



            try {
                //will serve as an accumlator and store every word space seperated

                String strContent;
                //will store each line's content

                while ((strContent = br.readLine()) != null ){//reads each line until "null" character is seen

                    strBoggleWords.add(strContent.toUpperCase());
                    //add each word to the ArrayList
                }

            } catch (IOException e) {//ensures anything past the end of the file isn't read
                e.printStackTrace();
            } finally {
                try {
                    br.close(); //closes the file
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }


        }

        //ACCEPTABLE WORDS IS SORTED ALPHABETICALLY
        Collections.sort(strBoggleWords);




        // https://stackoverflow.com/questions/20330355/timertask-or-handler

        final Handler handler = new Handler();

        TimerTask timertask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        TextView displayAttempt = (TextView) findViewById(R.id.attemptDisplay);
                        displayAttempt.setText("TIME'S UP!!!");

                        Button submit = (Button) findViewById(R.id.submit);
                        submit.setEnabled(false);


                        toEndScreen();
                    }
                });
            }
        };
        Timer timer = new Timer();
        timer.schedule(timertask, GAME_DURATION);




    }




    public void toEndScreen() {

        Intent intent = new Intent(this, endScreen.class);

        intent.putExtra("FinalScore", score);
        intent.putExtra("Multiplayer?", false);
        intent.putExtra("gameKey", -1);


        startActivity(intent);
    }




    // INITIALIZATION OF IMPORTANT VARIABLES!!!!!

    int [] buttonIds = {R.id.b00, R.id.b01, R.id.b02, R.id.b03,
                         R.id.b10, R.id.b11, R.id.b12, R.id.b13,
                         R.id.b20, R.id.b21, R.id.b22, R.id.b23,
                         R.id.b30, R.id.b31, R.id.b32, R.id.b33};
    // THIS ARRAY STORES ALL THE IDS OF THE BUTTONS ON THE GRID


    String [] lettersOnGrid = new String [16];

    String modAlphabet = "abcdefghijklmnopqrstuvwxyzaeiouaeiou".toUpperCase();

    static String strUpdate = "hi";

    int score = 0;
    // will store user's score


    String attemptedWord = "";
    //will store the word in progress


    ArrayList<int []> coordinatesPressed = new ArrayList<>();
    // will store the coordinates of letters in order of them being pressed


    static ArrayList<String> strBoggleWords = new ArrayList<String>();
    // will store the words in the dictionary


    static ArrayList<String> wordsEntered = new ArrayList<String>();
    // will store the words in the dictionary


    ArrayList<Button> buttonsPressed = new ArrayList<>();

    /**
     * called when a letter is pressed
     * @param view as a Button
     */
    public void buttonPressed(View view) {

        Button letterPressed = (Button) view;




        /*
        THE FOLLOWING CODE ADDS THE COORDINATE OF THE BUTTON PRESSED TO THE ARRAYLIST
         */
        String coordinatePressed = letterPressed.getResources().getResourceName(letterPressed.getId ()).substring(34);
        int [] coordinates = {Integer.parseInt((Character.toString(coordinatePressed.charAt(0)))),
                Integer.parseInt((Character.toString(coordinatePressed.charAt(0))))};

        coordinatesPressed.add(coordinates);




        TextView display = (TextView) findViewById(R.id.attemptDisplay);
        // STORE DISPLAY TEXTVIEW


        attemptedWord += letterPressed.getText();
        // update and display the attempted word
        display.setText("" + attemptedWord);



        letterPressed.setClickable(false);
        letterPressed.setTextColor(Color.RED);
        // DISABLE A BUTTON AFTER ITS PRESSED

        buttonsPressed.add(letterPressed);
        // add the button to the arraylist of buttons pressed



    }


    /**
     * displays random selections of the Alphabet onto the 16 buttons
     */
    public void setupGrid() {

        for (int id: buttonIds) {
            int randomIndex = (int) (Math.random()*16);
            // random number between 0 and 16


            String letterSelected = modAlphabet.charAt(randomIndex) + "";

            if (letterSelected.equals("Q")) letterSelected = "Qu";
            //in Boggle Q does not exist alone as a tile

            ((Button) findViewById(id)).setText(letterSelected);
            //set the buttons text to the random letter




        }



        //TextView scoreDisplay = (TextView) findViewById(R.id.hostScoreDisplay);
        //scoreDisplay.setText("Score: " + score);

        for (int i = 0; i < 16; i++) {
            lettersOnGrid[i] = (String) ((Button) findViewById(buttonIds[i])).getText();
        }

    }


    /**
     * SHUFFLES THE LETTERS ON THE GRID
     * @param view the shuffle button
     */
    public void shuffle(View view) {
        resetGrid();

        ArrayList <String> letters = new ArrayList<>();

        for (int buttonId : buttonIds) {
            letters.add((String) ((Button) findViewById(buttonId)).getText());
        }
        // ADDS EACH LETTER ON EACH BUTTON TO AN ARRAYLIST




        for (int i = 0; i < 16; i++) {

            int randomIndex = (int) (Math.random() * (16 - i));
            // generates index of letter to be drawn


            Button buttonToChange = (Button) findViewById(buttonIds[i]);
            // store the ith button in a variable



            buttonToChange.setText(letters.get(randomIndex));
            //set the text of the button to the random letter

            letters.remove(randomIndex);
            //remove the random letter from the sample space
        }
        // CHANGE THE TEXT OF EVERY BUTTON TO A RANDOMLY DRAWN LETTER, THEN REMOVE IT


    }


    /**
     *
     * @return true if the buttons were pressed in a valid sequence and false otherwise
     */
    public boolean isValidSequenceOfPressedButtons() {

        for (int i = 1; i < coordinatesPressed.size(); i++) {

            int [] point1 = coordinatesPressed.get(i);
            int [] point2 = coordinatesPressed.get(i - 1);
            // the two points to be compared

            int changeInRow = Math.abs(point2[0] - point1[0]);
            int changeInColumn = Math.abs(point2[1] - point1[1]);

            if ((changeInColumn > 1) || (changeInRow > 1)) return false;

        }

        return true;


    }


    /**
     * side effects
     * @param view as the button pressed
     */
    public void submit(View view) {

        TextView display = findViewById(R.id.attemptDisplay);


        // DID THE PLAYER WIN??j
        if (isValidWord(attemptedWord) && (attemptedWord.length() > 2) &&
                (! wordsEntered.contains(attemptedWord)) && isValidSequenceOfPressedButtons()) {
            success();
        } else {
            failure();
        }


        final ScrollView sv = (ScrollView) findViewById(R.id.SCROLLER_ID);
        sv.post(new Runnable() {
            public void run() {
                sv.fullScroll(View.FOCUS_DOWN);
            }
        });

    }

    /**
     *
     * @param attempt as the word the player is attempting
     * @return true if it is a valid entry and false otherwise
     */
    public boolean isValidWord(String attempt) {

        boolean isValid = isValidSequenceOfPressedButtons() && strBoggleWords.contains(attempt.toUpperCase()) && (attempt.length() > 2);
        return isValid;
    }


    /**
     * allows user to attempt another word
     */
    public void resetGrid() {


        attemptedWord="";
        for (Button button: buttonsPressed) {
            button.setClickable(true);
            button.setTextColor(Color.BLACK);
            // ENABLES A BUTTON
        }
        coordinatesPressed.clear();



        buttonsPressed.clear();


        attemptedWord = "";
        TextView attemptDisplay = (TextView) findViewById(R.id.attemptDisplay);
        //reset attempt and it's display
        attemptDisplay.setText("");


    }

    /**
     *
     * @param view
     */
    public void reset(View view) {
        resetGrid();
    }


    /**
     * called if the user correctly entered a word
     */
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






        TextView scoreDisplay = (TextView) findViewById(R.id.scoreDisplay);
        scoreDisplay.setText("Score: " + score);
        // updates score display

        TextView historyDisplay = (TextView) findViewById(R.id.historyDisplay);

        //Will update table of words already entered
        String strPast = historyDisplay.getText() + "";


        strUpdate = (strPast + "\n" + attemptedWord + " - " + intGain + "\n");


        historyDisplay.setText(strPast + "\n" + attemptedWord + " - " + intGain + "\n");

        coordinatesPressed.clear();
        //will clear the history of buttons pressed




        resetGrid();

    }


    // called when user's word is invalid
    public void failure() {


        TextView historyDisplay = (TextView) findViewById(R.id.historyDisplay);

        //Will update table of words already entered
        String strPast = historyDisplay.getText() + "";
        if (attemptedWord.length() != 0) historyDisplay.setText(strPast + "\ninvalid: " + attemptedWord + "\n");
        //output the message

        resetGrid();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    /**
     * Will set up all required listeners for the multiplayer modes (joining or hosting)
     * @param GAME_KEY as the host code for a multiplayer game
     * @param mBoardsDatabaseReference as a reference to the Boards child in the database reference
     */
    public void setupListeners(final String GAME_KEY, DatabaseReference mBoardsDatabaseReference) {


        DatabaseReference historyDatabaseReference = mBoardsDatabaseReference.child(GAME_KEY).child("Game in session").child("history");
        historyDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String updatedHistory = dataSnapshot.getValue(String.class);
                TextView historyDisplay = (TextView) findViewById(R.id.historyDisplay);
                historyDisplay.setText(updatedHistory);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });




        mBoardsDatabaseReference.child(GAME_KEY).child("Submitted Words").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {
                    @Override
                    public int hashCode() {
                        return super.hashCode();
                    }
                };
                Object[] update_data = dataSnapshot.getValue(t).toArray();
                wordsEntered.clear();

                for (Object datum : update_data) wordsEntered.add((String) datum);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // EXTRACT THE LETTERS ON GRID
        // THIS SHOULD BE CHANGED TO SINGLE VALUE EVENT LISTENER
        mBoardsDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


}


