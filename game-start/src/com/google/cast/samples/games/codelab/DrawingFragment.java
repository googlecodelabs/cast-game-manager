// Copyright 2015 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.cast.samples.games.codelab;

import com.google.android.gms.cast.games.GameManagerClient;
import com.google.android.gms.cast.games.GameManagerState;
import com.google.android.gms.cast.games.PlayerInfo;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

/**
 * Fragment for drawing.
 * Based on https://github.com/playgameservices/8bitartist
 */
public class DrawingFragment extends GameFragment
        implements DrawView.DrawViewListener, View.OnClickListener {

    private static final String TAG = "DrawingFragment";

    private static final int MAX_TIME = 30;
    private static final int MAX_WORDS = 10;

    // AlertDialog for showing messages to the user
    private AlertDialog mAlertDialog;

    // It is the player's turn when (match turn number % num participants == my turn index)
    private int mMyTurnIndex;

    // The match turn number, monotonically increasing from 0
    private int mMatchTurnNumber = 0;

    // The eligible guess words for this turn
    private List<String> mTurnWords;

    // The index of the correct word
    private int mWordIndex = 0;

    // Set of participant IDs for players that have guessed this turn
    private HashSet<String> mGuessersThisTurn = new HashSet<>();

    // True if this player has already guessed this turn, false otherwise
    private boolean mHasGuessed = false;

    // Data to draw the DrawView
    private DrawView mDrawView;

    // All possible words for game
    private String[] mAllWords;

    // ProgressBar, TextView, and Handler used to show the time remaining to make a guess.
    private ProgressBar mGuessProgress;
    private TextView mGuessProgressText;
    private Handler mGuessProgressHandler = new Handler(Looper.getMainLooper());

    private ListView mListView;
    private View mAristUIView;
    private View mGuesserUIView;
    private View mClearDoneLayoutView;
    private View mGuessWordView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment.
        View view = inflater.inflate(R.layout.drawing_fragment, container, false);

        // Set up guesser progress
        mGuessProgress = (ProgressBar) view.findViewById(R.id.guessProgress);
        mGuessProgressText = (TextView) view.findViewById(R.id.guessProgressText);

        // Button click listeners
        view.findViewById(R.id.clearButton).setOnClickListener(this);
        view.findViewById(R.id.doneButton).setOnClickListener(this);

        // ListView item click listener for word guessing
        mListView = (ListView) view.findViewById(R.id.listView);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                makeGuess(position);
            }
        });

        mDrawView = ((DrawView) view.findViewById(R.id.drawView));
        mDrawView.setListener(this);

        // Create array of all words
        mAllWords = getResources().getString(R.string.words).split("\\s*,\\s*");
        mTurnWords = Arrays.asList(mAllWords);

        mAristUIView = view.findViewById(R.id.artistUI);
        mGuesserUIView = view.findViewById(R.id.guesserUI);
        mClearDoneLayoutView = view.findViewById(R.id.clearDoneLayout);
        mGuessWordView = view.findViewById(R.id.guessWord);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        startMatch();
    }

    private void showDialog(String title, String message,
            DialogInterface.OnClickListener onClickListener) {
        if (getView() != null) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getView().getContext())
                    .setTitle(title)
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.game_dialog_ok_button_text),
                            onClickListener);

            mAlertDialog = alertDialogBuilder.create();
            mAlertDialog.show();
        }
    }

    /**
     * Begin a new match, send a message to all other participants with the initial turn data
     */
    private void startMatch() {
        mMatchTurnNumber = 0;
        mHasGuessed = false;
        mTurnWords = null;
        updateTurnIndices();
        if (isMyTurn()) {
            // Pick words randomly
            mTurnWords = getRandomWordSubset(10);
            mWordIndex = (new Random()).nextInt(mTurnWords.size());

            // Send turn message to others
            sendTurnMessage(0);
        }

        beginMyTurn();
    }

    private void sendTurnMessage(int matchTurnNumber) {
    }

    /**
     * Determines if the current player is drawing or guessing. Used to determine what UI to show
     * and what messages to send.
     *
     * @return true if the current player is the artist, false otherwise.
     */
    private boolean isMyTurn() {
        return true;
    }

    /**
     * Update the turn order so that each participant has a unique slot.
     */
    private void updateTurnIndices() {
    }

    @Override
    public void onDrawEvent(int gridX, int gridY, short colorIndex) {
    }

    /**
     * Clear the DrawView and send a message to receiver to do the same
     */
    private void onClearClicked() {
        mDrawView.clear();
    }

    /**
     * Create a Dialog with the result of the local player's guess.
     *
     * @param guessIndex   the index in the word list that the player clicked.
     * @param correctIndex the index in the word list of the correct answer.
     */
    private void createGuessDialog(int guessIndex, int correctIndex) {
        if (guessIndex == -1) {
            showDialog(getString(R.string.oh), getString(R.string.you_ran_out_of_time), null);
            return;
        }
        Log.d(TAG, "Guessed..." + mTurnWords.get(guessIndex));
        mHasGuessed = true;
        String guessedWord = mTurnWords.get(guessIndex);
        String correctWord = mTurnWords.get(correctIndex);

        if (guessIndex == correctIndex) {
            // The player guessed correctly
            showDialog(getString(R.string.you_got_it),
                    String.format(getResources().getString(R.string.is_correct), guessedWord),
                    null);
        } else {
            // The player guessed incorrectly
            showDialog(getString(R.string.no),
                    String.format(getResources().getString(R.string.is_wrong_the_real_answer_was),
                            guessedWord, correctWord), null);
        }
    }

    /**
     * Create a dialog with the result of another player's guess.
     *
     * @param guesserId the participant ID of the player that guessed.
     */
    private void createOpponentGuessDialog(String guesserId) {
    }

    /**
     * Show or hide the word choice list for guessing.
     *
     * @param enable true if the list should be shown, false otherwise.
     */
    private void enableGuessing(boolean enable) {
        if (getView() != null) {
            if (enable) {
                mListView.setVisibility(View.VISIBLE);
            } else {
                mListView.setVisibility(View.INVISIBLE);
            }
        }
    }

    /**
     * Begin a turn where the player is guessing what the artist is drawing. Begins a countdown
     * from MAX_TIME to 1 which determines how many points the player will get if and when they
     * make a correct guess,
     */
    private void beginGuessingTurn() {
        mDrawView.clear();
        mHasGuessed = false;
        setGuessingUI();

        // Set up the progress dialog
        mGuessProgress.setProgress(MAX_TIME);
        mGuessProgressText.setText(String.valueOf(MAX_TIME));

        // Decrement from MAX_TIME to 1, once every second
        Runnable decrementProgress = new Runnable() {
            @Override
            public void run() {
                if (!mHasGuessed) {
                    int oldProgress = mGuessProgress.getProgress();
                    if (oldProgress > 1) {
                        mGuessProgress.setProgress(oldProgress - 1);
                        mGuessProgressText.setText(String.valueOf(oldProgress - 1));
                        mGuessProgressHandler.postDelayed(this, 1000L);
                    } else {
                        // Time is up
                        makeGuess(-1);
                    }
                }
            }
        };
        mGuessProgressHandler.removeCallbacksAndMessages(null);
        mGuessProgressHandler.postDelayed(decrementProgress, 1000L);

        updateViewVisibility();

        if (mTurnWords == null) {
            // Send message to others about who is the new player
            sendPlayerMessage();
        }
    }

    private void sendPlayerMessage() {
    }

    /**
     * Begin a turn where the player is drawing. Clear the DrawView and show the drawing UI.
     */
    private void beginArtistTurn() {
        mDrawView.clear();
        // Send a message to the receiver to clear the drawing area
        sendClearMessage();

        // Send message to others about who is the artist
        sendArtistMessage();

        setArtistUI();
        updateViewVisibility();
    }

    private void sendClearMessage() {
    }

    private void sendArtistMessage() {
    }

    /** Begin the player's turn, calling the correct beginTurn function based on role **/
    private void beginMyTurn() {
        if (isMyTurn()) {
            beginArtistTurn();
        } else {
            beginGuessingTurn();
        }
    }

    /**
     * When the artist clicks done, all guessing is closed and the turn should be passed to the
     * next person to draw. The artist can do this at any point and the artist's turn is never over
     * until Done is clicked.
     */
    private void onDoneClicked() {
        // Increment turn number
        mMatchTurnNumber = mMatchTurnNumber + 1;

        // Choose random word subset and correct word
        mTurnWords = getRandomWordSubset(MAX_WORDS);
        mWordIndex = (new Random()).nextInt(mTurnWords.size());

        // Send new turn data to others
        sendTurnMessage(mMatchTurnNumber);

        beginMyTurn();
        updateViewVisibility();
    }

    /**
     * Pick a random set of words from the master word list.
     *
     * @param numWords the number of words to choose.
     * @return a list of randomly chosen words.
     */
    private List<String> getRandomWordSubset(int numWords) {
        List<String> result = new ArrayList<>();

        Collections.addAll(result, mAllWords);
        Collections.shuffle(result);
        result = result.subList(0, numWords);

        return result;
    }

    /**
     * Record my guess and inform all other players.
     *
     * @param position the index in the word list of my guess.
     */
    private void makeGuess(int position) {
        // Send my guess to other players
        sendGuessMessage(position);

        // Disable guessing and show result
        enableGuessing(false);
        createGuessDialog(position, mWordIndex);
    }

    private void sendGuessMessage(int position) {
    }

    /**
     * Show the UI for a non-artist player
     */
    private void setGuessingUI() {
        mAristUIView.setVisibility(View.GONE);
        mGuesserUIView.setVisibility(View.VISIBLE);
        mClearDoneLayoutView.setVisibility(View.GONE);
        mGuessWordView.setVisibility(View.GONE);

        // Disable touch on drawview
        mDrawView.setTouchEnabled(false);
        mDrawView.setVisibility(View.GONE);
        enableGuessing(true);

        // Set words, clear draw view
        resetWords(mTurnWords);
        mDrawView.clear();
    }

    /**
     * Show the UI for the player who is currently acting as the artist.
     */
    private void setArtistUI() {
        mAristUIView.setVisibility(View.VISIBLE);
        mGuesserUIView.setVisibility(View.GONE);
        mClearDoneLayoutView.setVisibility(View.VISIBLE);
        mGuessWordView.setVisibility(View.VISIBLE);

        mDrawView.setTouchEnabled(true);
        mDrawView.setVisibility(View.VISIBLE);

        ((TextView) getView().findViewById(R.id.guessWord)).setText(mTurnWords.get(mWordIndex));
        mDrawView.clear();
    }

    /**
     * Set the list of words to display for guessing.
     */
    private void resetWords(List<String> words) {
        if (words != null) {
            ListView list = (ListView) getView().findViewById(R.id.listView);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getView().getContext(),
                    R.layout.list_item, R.id.text, words);
            list.setAdapter(adapter);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clearButton:
                onClearClicked();
                break;
            case R.id.doneButton:
                onDoneClicked();
                break;
        }
    }

    private void updateViewVisibility() {
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Set UI for either artist or guesser based on turn
        if (isMyTurn()) {
            setArtistUI();
        } else {
            setGuessingUI();
        }
    }

    /**
     * Game state callback.
     */
    public void onStateChanged(GameManagerState newState, GameManagerState oldState) {
    }

    /**
     * Game message callback.
     *
     * @param playerId ID of player who sent the message
     * @param message  JSON message
     */
    public void onGameMessageReceived(String playerId, JSONObject message) {
    }

    private void sendGameMessage(JSONObject jsonObject) {
    }
}
