package com.example.android.boggleapp;

import java.util.ArrayList;
import java.util.List;

public class Board {

    public ArrayList<String> wordsEntered;
    // will store the words in the dictionary


    public String lettersOnGrid;

    public String history;

    public int scoreHost;

    public int scoreJoinee;

//

    Board(java.util.ArrayList<java.lang.String> wordsEntered, String lettersOnGrid, String history, int scoreHost, int scoreJoinee) {
        this.wordsEntered = wordsEntered;
        this.lettersOnGrid = lettersOnGrid;
        this.history = history;
        this.scoreHost = scoreHost;
        this.scoreJoinee = scoreJoinee;

    }




}
