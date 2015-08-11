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
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Observable;
import java.util.Observer;

/**
 * The main activity. Fragments are loaded for the various stages in the UI.
 */
public class MainActivity extends AppCompatActivity implements Observer {

    private static final String TAG = "MainActivity";

    private CastConnectionFragment mCastConnectionFragment;
    private LobbyFragment mLobbyFragment;
    private DrawingFragment mDrawingFragment;
    private int mPlayerState = GameManagerClient.PLAYER_STATE_UNKNOWN;
    private String mPlayerName;

    private CastConnectionManager mCastConnectionManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        // Set up Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mCastConnectionManager = new CastConnectionManager(this,
                getResources().getString(R.string.app_id));
        mCastConnectionFragment = new CastConnectionFragment();
        mDrawingFragment = new DrawingFragment();
        mLobbyFragment = new LobbyFragment();

        updateFragments();
    }

    public CastConnectionManager getCastConnectionManager() {
        return mCastConnectionManager;
    }

    /**
     * Called when the options menu is first created.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item);
        MediaRouteActionProvider mediaRouteActionProvider =
                (MediaRouteActionProvider) MenuItemCompat.getActionProvider(mediaRouteMenuItem);
        if (mediaRouteActionProvider == null) {
            Log.w(TAG, "mediaRouteActionProvider is null!");
            return false;
        }
        mediaRouteActionProvider.setRouteSelector(mCastConnectionManager.getMediaRouteSelector());
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCastConnectionManager.startScan();
        mCastConnectionManager.addObserver(this);
        updateFragments();
    }

    @Override
    protected void onPause() {
        mCastConnectionManager.stopScan();
        mCastConnectionManager.deleteObserver(this);
        super.onPause();
    }

    /**
     * Called when the cast connection changes.
     */
    @Override
    public void update(Observable object, Object data) {
        final GameManagerClient gameManagerClient = mCastConnectionManager.getGameManagerClient();
        if (mCastConnectionManager.isConnectedToReceiver()) {
            PendingResult<GameManagerClient.GameManagerResult> result =
                    gameManagerClient.sendPlayerAvailableRequest(null);
            result.setResultCallback(new ResultCallback<GameManagerClient.GameManagerResult>() {
                @Override
                public void onResult(final GameManagerClient.GameManagerResult gameManagerResult) {
                    if (gameManagerResult.getStatus().isSuccess()) {
                        Log.d(TAG, "Player ID: " + gameManagerResult.getPlayerId());
                        mPlayerState = gameManagerClient.getCurrentState().getPlayer(
                                gameManagerResult.getPlayerId()).getPlayerState();
                    } else {
                        mCastConnectionManager.disconnectFromReceiver(false);
                        Utils.showErrorDialog(MainActivity.this,
                                gameManagerResult.getStatus().getStatusMessage());
                    }
                    updateFragments();
                }
            });
        }
        updateFragments();
    }

    private void updateFragments() {
        if (isChangingConfigurations() || isFinishing() || isDestroyed()) {
            return;
        }

        Fragment fragment;
        if (!mCastConnectionManager.isConnectedToReceiver()) {
            mPlayerName = null;
            fragment = mCastConnectionFragment;
        } else {
            if (mPlayerState == GameManagerClient.PLAYER_STATE_PLAYING) {
                fragment = mDrawingFragment;
            } else {
                fragment = mLobbyFragment;
            }
        }
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commitAllowingStateLoss();
    }

    @Override
    public void onBackPressed() {
        // Do not pop any fragments, just act like the home button.
        moveTaskToBack(true);
    }

    public int getPlayerState() {
        return mPlayerState;
    }

    public void setPlayerState(int state) {
        mPlayerState = state;
        updateFragments();
    }

    public String getPlayerName() {
        return mPlayerName;
    }

    public void setPlayerName(String playerName) {
        mPlayerName = playerName;
    }
}
