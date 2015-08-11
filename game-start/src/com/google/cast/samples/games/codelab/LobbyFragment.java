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
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

/**
 * A fragment displayed while the player is in the game lobby.
 */
public class LobbyFragment extends GameFragment {

    private static final String TAG = "LobbyFragment";

    private EditText mNameEditText;
    private Button mJoinStartButton;
    private ProgressBar mSpinner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment.
        View view = inflater.inflate(R.layout.lobby_fragment, container, false);

        mNameEditText = (EditText) view.findViewById(R.id.name);
        mSpinner = (ProgressBar) view.findViewById(R.id.spinner);
        mJoinStartButton = (Button) view.findViewById(R.id.button_join_start);
        mJoinStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onJoinStartClicked();
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateView();
    }

    /**
     * Button click handler. Set the new player state based on the current player state.
     */
    private void onJoinStartClicked() {
        updateView();
    }

    /**
     * Change the player state to PLAYER_STATE_READY.
     */
    public void sendPlayerReadyRequest() {
        updateView();
    }

    /**
     * Change the player state to PLAYER_STATE_PLAYING.
     */
    public void sendPlayerPlayingRequest() {
        updateView();
    }

    /**
     * Update the UI based on the current lobby and player state. The player has to first join
     * the lobby and then start the game.
     */
    private void updateView() {
        if (((MainActivity) getActivity()).getPlayerName() == null) {
            mNameEditText.setText("");
        }
        mJoinStartButton.setVisibility(View.VISIBLE);
        mSpinner.setVisibility(View.GONE);
    }

}
