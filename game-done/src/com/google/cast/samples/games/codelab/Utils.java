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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

/**
 * Global utility methods.
 */
public class Utils {

    /**
     * Shows an error dialog.
     *
     * @param errorMessage The message to show in the dialog.
     */
    public static void showErrorDialog(final Activity activity, final String errorMessage) {
        if (!activity.isDestroyed()) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Show a error dialog along with error messages.
                    AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
                    alertDialog
                            .setTitle(activity.getString(R.string.game_connection_error_message));
                    alertDialog.setMessage(errorMessage);
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL,
                            activity.getString(R.string.game_dialog_ok_button_text),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
            });
        }
    }

}
