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
  /** @private {!cast.receiver.games.GameManager} */
  this.gameManager_ = gameManager;

  // Cast debugging
  cast.receiver.logger.setLevelValue(cast.receiver.LoggerLevel.DEBUG);

  /**
   * Game Manager Debug only. Call debugUi.open() or close() to show and hide an overlay
   * showing game manager and player information while testing and debugging.
   * @public {cast.receiver.games.debug.DebugUI}
   */
  this.debugUi = new cast.receiver.games.debug.DebugUI(this.gameManager_);

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

  this.gameManager_.removeEventListener(
      cast.receiver.games.EventType.PLAYER_READY,
      this.boundPlayerReadyCallback_);
  this.gameManager_.removeEventListener(
      cast.receiver.games.EventType.PLAYER_PLAYING,
      this.boundPlayerPlayingCallback_);
  this.gameManager_.removeEventListener(
      cast.receiver.games.EventType.GAME_MESSAGE_RECEIVED,
      this.boundGameMessageCallback_);
  this.gameManager_.removeEventListener(
      cast.receiver.games.EventType.PLAYER_QUIT,
      this.boundPlayerQuitCallback_);
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
  this.gameManager_.updateGameplayState(
      cast.receiver.games.GameplayState.RUNNING, null);

  this.loadedCallback_();
  this.loadedCallback_ = null;

  // Listen to when a player is ready or starts playing.
  this.gameManager_.addEventListener(
      cast.receiver.games.EventType.PLAYER_READY,
      this.boundPlayerReadyCallback_);
  this.gameManager_.addEventListener(
      cast.receiver.games.EventType.PLAYER_PLAYING,
      this.boundPlayerPlayingCallback_);
  this.gameManager_.addEventListener(
      cast.receiver.games.EventType.GAME_MESSAGE_RECEIVED,
      this.boundGameMessageCallback_);
  this.gameManager_.addEventListener(
      cast.receiver.games.EventType.PLAYER_QUIT,
      this.boundPlayerQuitCallback_);

  // The game is showing lobby screen and the lobby is open for new players.
  this.gameManager_.updateGameplayState(
      cast.receiver.games.GameplayState.SHOWING_INFO_SCREEN, null);
  this.gameManager_.updateLobbyState(cast.receiver.games.LobbyState.OPEN, null);
  this.updateTitle_('Lobby');

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
  if (!this.isSuccessEvent_(event)) {
    return;
  }
  var playerId = /** @type {string} */ (event.playerInfo.playerId);
  var playerName = event.requestExtraMessageData.name;
  console.log('Player is ready: ' + playerName);
  this.updateInfo_(playerName + ' has joined.');
  this.players_[playerId] = playerName;
};

/**
 * Handles when a player becomes playing to the game manager.
 * @param {cast.receiver.games.Event} event
 * @private
 */
Game.prototype.onPlayerPlaying_ =
    function(event) {
  if (!this.isSuccessEvent_(event)) {
    return;
  }
  var playerId = /** @type {string} */ (event.playerInfo.playerId);
  // Update all ready players to playing state.
  var players = this.gameManager_.getPlayers();
  for (var i = 0; i < players.length; i++) {
    if (players[i].playerState == cast.receiver.games.PlayerState.READY) {
      this.gameManager_.updatePlayerState(players[i].playerId,
          cast.receiver.games.PlayerState.PLAYING, null);
    }
  }
  this.gameManager_.updateGameplayState(cast.receiver.games.GameplayState.RUNNING, null);
  this.gameManager_.updateLobbyState(cast.receiver.games.LobbyState.CLOSED, null);
  this.updateTitle_('Playing');
  this.updateInfo_(this.players_[playerId] + ' is playing.');
};


/**
 * Handles when a player disconnects from the game manager.
 * @param {cast.receiver.games.Event} event
 * @private
 */
Game.prototype.onPlayerQuit_ =
    function(event) {
  if (!this.isSuccessEvent_(event)) {
    return;
  }
  // Tear down the game if there are no more players. Might want to show a nice
  // UI with a countdown instead of tearing down instantly.
  var connectedPlayers = this.gameManager_.getConnectedPlayers();
  console.log('Connected players=' + connectedPlayers.length);
  if (connectedPlayers.length == 0) {
    console.log('No more players connected. Tearing down game.');
    cast.receiver.CastReceiverManager.getInstance().stop();
  }
};


/**
 * Callback for game message sent via game manager.
 * @param {cast.receiver.games.Event} event
 * @private
 */
Game.prototype.onGameMessage_ =
    function(event) {
  if (!this.isSuccessEvent_(event)) {
    return;
  }
  var playerId = /** @type {string} */ (event.playerInfo.playerId);
  var message = event.requestExtraMessageData;
  console.log("message=" + message);
  if (message.clear) {
    this.clearGrid_();
    return;
  }
  if (message.artist) {
    this.updateInfo_(this.players_[message.artist] + ' is drawing.');
    return;
  }
  if (message.player) {
    if (this.wordsMessage_) {
      this.gameManager_.sendGameMessageToPlayer(playerId, this.wordsMessage_);
    }
    return;
  }
  if (message.words) {
    this.gameManager_.sendGameMessageToAllConnectedPlayers(message);
    this.wordsMessage_ = message;
    return;
  }
  if (message.guess) {
    this.gameManager_.sendGameMessageToAllConnectedPlayers(message);
    return;
  }
  var element = document.getElementById(message.grid);
  if (element) {
   element.style.backgroundColor = 'blue';
  }
};

/**
 * Utility method to check if game event status is SUCCESS.
 * @param {cast.receiver.games.Event} event
 * @private
 */
Game.prototype.isSuccessEvent_ = function(event) {
  if (event.statusCode != cast.receiver.games.StatusCode.SUCCESS) {
    console.log('Error: Event status code: ' + event.statusCode);
    console.log('Reason for error: ' + event.errorDescription);
    return false;
  }
  return true;
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