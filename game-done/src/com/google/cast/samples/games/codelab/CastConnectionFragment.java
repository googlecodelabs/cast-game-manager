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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Observable;

/**
 * A fragment displayed while this application is not yet connected to a cast device.
 */
public class CastConnectionFragment extends GameFragment {

    public static final String TAG = "CastConnectionFragment";

    private View mConnectLabel;
    private View mSpinner;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cast_connection_fragment, container, false);
        mConnectLabel = view.findViewById(R.id.connect_label);
        mSpinner = view.findViewById(R.id.spinner);
        return view;
    }

    @Override
    public void update(Observable object, Object data) {
        if (getView() == null) {
            return;
        }
        // Once the user has selected a Cast device, show a progress indicator.
        // MainActivity will load the lobby fragment next.
        if (mCastConnectionManager.getSelectedDevice() != null) {
            mConnectLabel.setVisibility(View.GONE);
            mSpinner.setVisibility(View.VISIBLE);
        } else {
            mConnectLabel.setVisibility(View.VISIBLE);
            mSpinner.setVisibility(View.GONE);
        }
    }
}
