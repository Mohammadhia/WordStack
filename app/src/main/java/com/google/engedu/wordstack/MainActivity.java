package com.google.engedu.wordstack;

import android.content.res.AssetManager;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    private static final int WORD_LENGTH = 5;
    public static final int LIGHT_BLUE = Color.rgb(176, 200, 255);
    public static final int LIGHT_GREEN = Color.rgb(200, 255, 200);
    private ArrayList<String> words = new ArrayList<>();
    private Random random = new Random();
    private StackedLayout stackedLayout;
    private String word1, word2;
    private Stack<LetterTile> placedTiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AssetManager assetManager = getAssets();

        placedTiles = new Stack<LetterTile>();//Instantiates stack for placedTiles

        try {
            InputStream inputStream = assetManager.open("words.txt");
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            while((line = in.readLine()) != null) {
                String word = line.trim();

                if(word.length() == 5){
                    words.add(word);
                }

            }
        } catch (IOException e) {
            Toast toast = Toast.makeText(this, "Could not load dictionary", Toast.LENGTH_LONG);
            toast.show();
        }
        LinearLayout verticalLayout = (LinearLayout) findViewById(R.id.vertical_layout);
        stackedLayout = new StackedLayout(this);
        verticalLayout.addView(stackedLayout, 3);

        View word1LinearLayout = findViewById(R.id.word1);
        word1LinearLayout.setOnTouchListener(new TouchListener());
        //word1LinearLayout.setOnDragListener(new DragListener());
        View word2LinearLayout = findViewById(R.id.word2);
        word2LinearLayout.setOnTouchListener(new TouchListener());
        //word2LinearLayout.setOnDragListener(new DragListener());
    }

    private class TouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN && !stackedLayout.empty()) {
                LetterTile tile = (LetterTile) stackedLayout.peek();
                tile.moveToViewGroup((ViewGroup) v);
                if (stackedLayout.empty()) {
                    TextView messageBox = (TextView) findViewById(R.id.message_box);
                    messageBox.setText(word1 + " " + word2);
                }

                placedTiles.push(tile);

                return true;
            }
            return false;
        }
    }

    private class DragListener implements View.OnDragListener {

        public boolean onDrag(View v, DragEvent event) {
            int action = event.getAction();
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    v.setBackgroundColor(LIGHT_BLUE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    v.setBackgroundColor(LIGHT_GREEN);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    v.setBackgroundColor(LIGHT_BLUE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    v.setBackgroundColor(Color.WHITE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DROP:
                    // Dropped, reassign Tile to the target Layout
                    LetterTile tile = (LetterTile) event.getLocalState();
                    tile.moveToViewGroup((ViewGroup) v);
                    if (stackedLayout.empty()) {
                        TextView messageBox = (TextView) findViewById(R.id.message_box);
                        messageBox.setText(word1 + " " + word2);
                    }
                    /**
                     **
                     **  YOUR CODE GOES HERE
                     **
                     **/
                    return true;
            }
            return false;
        }
    }

    protected boolean onStartGame(View view) {
        TextView messageBox = (TextView) findViewById(R.id.message_box);
        messageBox.setText("Game started");

        word1 = words.get(random.nextInt(words.size()));
        word2 = words.get(random.nextInt(words.size()));

        int totalLetters = word1.length() + word2.length();
        int word1LengthCounter = word1.length();
        int word2LengthCounter = word2.length();

        Stack mixedLetters = new Stack();

        for(int i = 0; i < totalLetters; i++){
            if(word1LengthCounter > 0 && word2LengthCounter > 0){
                int randomLetter = random.nextInt(2);
                if(randomLetter == 0){
                    mixedLetters.push(word1.charAt(word1LengthCounter-1));
                    word1LengthCounter--;
                }
                else if(randomLetter == 1){
                    mixedLetters.push(word2.charAt(word2LengthCounter-1));
                    word2LengthCounter--;
                }
            }
            else if(word1LengthCounter > 0){
                mixedLetters.push(word1.charAt(word1LengthCounter-1));
                word1LengthCounter--;
            }
            else if(word2LengthCounter > 0){
                mixedLetters.push(word2.charAt(word2LengthCounter-1));
                word2LengthCounter--;
            }
        }

        String scrambledWords = "";
        while(!mixedLetters.empty()){
            scrambledWords += mixedLetters.pop();
        }

        for(int i = scrambledWords.length()-1; i >= 0; i--){
            LetterTile letter = new LetterTile(MainActivity.this, scrambledWords.charAt(i));
            stackedLayout.push(letter);
        }

        messageBox.setText(scrambledWords + "\n" + word1 + " " + word2);

        return true;
    }

    protected boolean onUndo(View view) {
        if(placedTiles.empty()) { //If there are no tiles to be removed, returns false so that when undo is clicked the app doesn't crash
            return false;
        } else {
            LetterTile tile = placedTiles.pop();
            tile.moveToViewGroup(stackedLayout);

            return true;
        }
    }
}
