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

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.Cast.ApplicationConnectionResult;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.games.GameManagerClient;
import com.google.android.gms.cast.games.GameManagerClient.GameManagerInstanceResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.support.v7.media.MediaRouter.RouteInfo;
import android.util.Log;

import java.util.Observable;

/**
 * Abstracts all the logic needed to establish a connection to a cast device and get the
 * GameManagerClient initialized.
 */
public class CastConnectionManager extends Observable {

    private static final String TAG = "CastConnectionManager";

    private final Context mContext;
    private final String mAppId;
    private final MediaRouter mMediaRouter;
    private final MediaRouteSelector mMediaRouteSelector;
    private final MediaRouter.Callback mMediaRouteCallback;
    private CastDevice mSelectedDevice;

    private GoogleApiClient mApiClient;

    private String mCastSessionId;
    private GameManagerClient mGameManagerClient;

    public CastConnectionManager(Context context, String appId) {
        mContext = context;
        mAppId = appId;

        mMediaRouter = MediaRouter.getInstance(context);

        mMediaRouteSelector = new MediaRouteSelector.Builder().addControlCategory(
                CastMediaControlIntent.categoryForCast(mAppId)).build();

        // Create a MediaRouter callback for discovery events.
        mMediaRouteCallback = new MediaRouteCallback();
    }

    /**
     * Returns the GameManagerClient instance, if already connected to the receiver, or null if
     * not.
     *
     * @see #isConnectedToReceiver
     */
    public GameManagerClient getGameManagerClient() {
        return mGameManagerClient;
    }

    /**
     * Returns true if there is an active connection to the receiver and the GameManagerClient is
     * ready to be used.
     */
    public boolean isConnectedToReceiver() {
        return (getGameManagerClient() != null) && (!getGameManagerClient().isDisposed());
    }

    /**
     * Returns the MediaRouteSelector so that it can be passed to a MediaRouteActionProvider.
     */
    public MediaRouteSelector getMediaRouteSelector() {
        return mMediaRouteSelector;
    }

    /**
     * Adds the callback to start device discovery. This method should be called from onResume of
     * the main activity.
     */
    public void startScan() {
        mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouteCallback,
                MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN);
    }

    /**
     * Removes the callback to stop device discovery. This method should be called from onPause of
     * the main activity.
     */
    public void stopScan() {
        mMediaRouter.removeCallback(mMediaRouteCallback);
    }

    /**
     * Utility method to disconnect from the receiver device.
     *
     * @param stopReceiverApplication Whether to stop the remote application even if there are
     *                                other senders connected.
     */
    public void disconnectFromReceiver(boolean stopReceiverApplication) {
        if (isApiClientConnected() && (mCastSessionId != null)) {
            if (stopReceiverApplication) {
                Cast.CastApi.stopApplication(mApiClient, mCastSessionId);
            } else {
                Cast.CastApi.leaveApplication(mApiClient);
            }
        }
        setSelectedDevice(null);
    }

    /**
     * Returns the CastDevice selected by the user, regardless of the connection status, or null if
     * no device has been selected.
     */
    public CastDevice getSelectedDevice() {
        return mSelectedDevice;
    }

    private void connectApiClient() {
        Cast.CastOptions apiOptions = Cast.CastOptions.builder(mSelectedDevice, new CastListener())
                .build();
        GoogleApiClientConnectionCallback callback = new GoogleApiClientConnectionCallback();
        mApiClient = new GoogleApiClient.Builder(mContext)
                .addApi(Cast.API, apiOptions)
                .addConnectionCallbacks(callback)
                .addOnConnectionFailedListener(callback)
                .build();
        mApiClient.connect();
    }

    private void disconnectApiClient() {
        if (mGameManagerClient != null) {
            mGameManagerClient.dispose();
            mGameManagerClient = null;
        }
        if (mApiClient != null && (mApiClient.isConnected() || mApiClient.isConnecting())) {
            mApiClient.disconnect();
        }
        mApiClient = null;
        setChanged();
        notifyObservers();
    }

    private void setSelectedDevice(CastDevice device) {
        Log.d(TAG, "setSelectedDevice: " + device);
        mSelectedDevice = device;

        // This will notify observers, so no need to explicitly do it in this method.
        disconnectApiClient();

        if (mSelectedDevice != null) {
            try {
                connectApiClient();
            } catch (IllegalStateException e) {
                Log.e(TAG, "Exception while connecting Google API client. ", e);
                disconnectApiClient();
            }
        } else {
            mMediaRouter.selectRoute(mMediaRouter.getDefaultRoute());
        }
    }

    /**
     * Media router callbacks.
     */
    private class MediaRouteCallback extends MediaRouter.Callback {

        @Override
        public void onRouteAdded(MediaRouter router, RouteInfo route) {
        }

        @Override
        public void onRouteRemoved(MediaRouter router, RouteInfo route) {
        }

        @Override
        public void onRouteSelected(MediaRouter router, RouteInfo info) {
            Log.d(TAG, "MediaRouteCallback.onRouteSelected: info=" + info);
            CastDevice device = CastDevice.getFromBundle(info.getExtras());
            setSelectedDevice(device);
        }

        @Override
        public void onRouteUnselected(MediaRouter router, RouteInfo info) {
            Log.d(TAG, "MediaRouteCallback.onRouteUnselected: info=" + info);
            setSelectedDevice(null);
        }
    }

    /**
     * Google API Client callbacks.
     */
    private class GoogleApiClientConnectionCallback implements
            GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

        @Override
        public void onConnectionSuspended(int cause) {
            Log.d(TAG, "GoogleApiClient disconnected. Cause: " + cause);
            setSelectedDevice(null);
        }

        @Override
        public void onConnected(Bundle connectionHint) {
            Log.d(TAG, "GoogleApiClient connected.");
            if (!isApiClientConnected()) {
                Log.w(TAG, "Got GoogleApiClient.onConnected callback but the Google API client is "
                        + "disconnected.");
                setSelectedDevice(null);
                return;
            }
            Cast.CastApi.launchApplication(mApiClient, mAppId)
                    .setResultCallback(new LaunchReceiverApplicationResultCallback());
        }

        @Override
        public void onConnectionFailed(ConnectionResult result) {
            Log.d(TAG, "Failed to connect the Google API client " + result);
            setSelectedDevice(null);
        }
    }

    /**
     * Cast API callbacks.
     */
    private class CastListener extends Cast.Listener {

        @Override
        public void onApplicationDisconnected(int statusCode) {
            Log.d(TAG, "Cast.Listener.onApplicationDisconnected: " + statusCode);
            setSelectedDevice(null);
        }
    }

    private final class LaunchReceiverApplicationResultCallback implements
            ResultCallback<ApplicationConnectionResult> {

        @Override
        public void onResult(ApplicationConnectionResult result) {
            Status status = result.getStatus();
            if (status.isSuccess()) {
                ApplicationMetadata appMetaData = result.getApplicationMetadata();
                Log.d(TAG, "Launching game: " + appMetaData.getName());
                mCastSessionId = result.getSessionId();
                GameManagerClient.getInstanceFor(mApiClient, mCastSessionId).setResultCallback(
                        new GameManagerGetInstanceCallback());
            } else {
                Log.d(TAG, "Unable to launch the the game. statusCode: " + status.getStatusCode());
                setSelectedDevice(null);
            }
        }
    }

    /**
     * GameManagerClient initialization callback.
     */
    private final class GameManagerGetInstanceCallback implements
            ResultCallback<GameManagerInstanceResult> {

        @Override
        public void onResult(GameManagerInstanceResult gameManagerResult) {
            if (!gameManagerResult.getStatus().isSuccess()) {
                Log.d(TAG, "Unable to initialize the GameManagerClient: "
                        + gameManagerResult.getStatus().getStatusMessage()
                        + " Status code: " + gameManagerResult.getStatus().getStatusCode());
                setSelectedDevice(null);
                return;
            }
            mGameManagerClient = gameManagerResult.getGameManagerClient();
            setChanged();
            notifyObservers();
        }
    }

    /**
     * @return True if the Google Api Client is connected.
     */
    private boolean isApiClientConnected() {
        return (mApiClient != null) && (mApiClient.isConnected());
    }
}
