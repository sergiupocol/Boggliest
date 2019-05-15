package com.example.android.boggleapp;

import java.util.ArrayList;
import java.util.List;

public class Board {



    public String lettersOnGrid;

    public String history;

    public int scoreHost;

    public int scoreJoinee;

//

    Board( String lettersOnGrid, String history, int scoreHost, int scoreJoinee) {
        this.lettersOnGrid = lettersOnGrid;
        this.history = history;
        this.scoreHost = scoreHost;
        this.scoreJoinee = scoreJoinee;

    }




}
