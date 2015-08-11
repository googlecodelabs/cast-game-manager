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

import org.json.JSONObject;

import android.app.Fragment;
import android.os.Bundle;

import java.util.Observable;
import java.util.Observer;

/**
 * A base class for all the fragments in the game.
 */
public class GameFragment extends Fragment implements Observer, GameManagerClient.Listener {

    private static final String TAG = "GameFragment";

    protected CastConnectionManager mCastConnectionManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mCastConnectionManager = ((MainActivity) getActivity()).getCastConnectionManager();
        mCastConnectionManager.addObserver(this);
        if (mCastConnectionManager.getGameManagerClient() != null) {
            mCastConnectionManager.getGameManagerClient().setListener(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCastConnectionManager.deleteObserver(this);
    }

    /**
     * GameManagerClient observer callback.
     */
    @Override
    public void update(Observable object, Object data) {
        // no-op
    }

    /**
     * Game state callback handler.
     */
    @Override
    public void onStateChanged(GameManagerState newState,
            GameManagerState oldState) {
        // no-op
    }

    /**
     * Game message callback handler.
     */
    @Override
    public void onGameMessageReceived(String playerId, JSONObject message) {
        // no-op
    }
}
