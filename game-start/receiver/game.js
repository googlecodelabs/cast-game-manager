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

/**
 * Codelab Demo game.
 *
 * Adds a sprite from a pool of sprites when a sender sends a custom game
 * message. Automatically transitions AVAILABLE players to the PLAYING state.
 *
 * @param {!cast.receiver.games.GameManager} gameManager
 * @constructor
 * @implements {cast.games.common.receiver.Game}
 * @export
 */
Game = function(gameManager) {

  /** @private {?function()} Callback used with #run. */
  this.loadedCallback_ = null;

  /** @private {boolean} */
  this.isLoaded_ = false;

  /** @private {boolean} */
  this.isRunning_ = false;

  /**
   * Pre-bound custom message callback.
   * @private {function(cast.receiver.games.Event)}
   */
  this.boundGameMessageCallback_ = this.onGameMessage_.bind(this);

  /**
   * Pre-bound player ready callback.
   * @private {function(cast.receiver.games.Event)}
   */
  this.boundPlayerReadyCallback_ = this.onPlayerReady_.bind(this);

 /**
  * Pre-bound player playing callback.
  * @private {function(cast.receiver.games.Event)}
  */
 this.boundPlayerPlayingCallback_ = this.onPlayerPlaying_.bind(this);

  /**
   * Pre-bound player quit callback.
   * @private {function(cast.receiver.games.Event)}
   */
  this.boundPlayerQuitCallback_ = this.onPlayerQuit_.bind(this);
};


/**
 * Runs the game. Game should load if not loaded yet.
 * @param {function()} loadedCallback This function will be called when the game
 *     finishes loading or is already loaded and about to actually run.
 * @export
 */
Game.prototype.run = function(loadedCallback) {
  // If the game is already running, return immediately.
  if (this.isRunning_) {
    loadedCallback();
    return;
  }

  // Start loading if game not loaded yet.
  this.loadedCallback_ = loadedCallback;

  // Start running.
  this.start_();
};


/**
 * Stops the game.
 * @export
 */
Game.prototype.stop = function() {
  if (this.loadedCallback_ || !this.isRunning_) {
    this.loadedCallback_ = null;
    return;
  }

  this.isRunning_ = false;
};


/**
 * Adds the renderer and run the game. Calls loaded callback passed to #run.
 * @private
 */
Game.prototype.start_ = function() {
  // If callback is null, the game was stopped already.
  if (!this.loadedCallback_) {
    return;
  }

  this.isRunning_ = true;

  this.loadedCallback_();
  this.loadedCallback_ = null;

  this.players_ = [];
  this.wordsMessage_ = null;
};


/**
 * Handles when a player becomes ready to the game manager.
 * @param {cast.receiver.games.Event} event
 * @private
 */
Game.prototype.onPlayerReady_ =
    function(event) {
};

/**
 * Handles when a player becomes playing to the game manager.
 * @param {cast.receiver.games.Event} event
 * @private
 */
Game.prototype.onPlayerPlaying_ =
    function(event) {
};


/**
 * Handles when a player disconnects from the game manager.
 * @param {cast.receiver.games.Event} event
 * @private
 */
Game.prototype.onPlayerQuit_ =
    function(event) {
};


/**
 * Callback for game message sent via game manager.
 * @param {cast.receiver.games.Event} event
 * @private
 */
Game.prototype.onGameMessage_ =
    function(event) {
};

/**
 * Utility method to check if game event status is SUCCESS.
 * @param {cast.receiver.games.Event} event
 * @private
 */
Game.prototype.isSuccessEvent_ = function(event) {
}

Game.prototype.clearGrid_ = function() {
  var tds = document.getElementsByTagName('td');
  for(var i=0, td=tds.length; i<td; i++){
     tds[i].style.backgroundColor = 'black';
  }
}

Game.prototype.updateInfo_ = function(message) {
  var info = document.getElementById('info');
  if (info) {
    info.textContent = message;
  }
}

Game.prototype.updateTitle_ = function(message) {
  var title = document.getElementById('title');
  if (title) {
    title.textContent = message;
  }
}