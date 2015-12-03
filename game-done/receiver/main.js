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
'use strict';

var castReceiverManager = cast.receiver.CastReceiverManager.getInstance();
var appConfig = new cast.receiver.CastReceiverManager.Config();

appConfig.statusText = 'Game is starting.';
// In production, use the default maxInactivity instead of using this.
appConfig.maxInactivity = 6000;

// Create the game before starting castReceiverManager to make sure any extra
// cast namespaces can be set up.
/** @suppress {missingRequire} */
var gameConfig = new cast.receiver.games.GameManagerConfig();
gameConfig.applicationName = 'Game';

/** @suppress {missingRequire} */
var gameManager = new cast.receiver.games.GameManager(gameConfig);

/** @suppress {missingRequire} */
var game = new Game(gameManager);

var startGame = function() {
  game.run(function() {
    console.log('Game running.');
    castReceiverManager.setApplicationState('Game running.');
  });
};

castReceiverManager.onReady = function(event) {
  if (document.readyState === 'complete') {
    startGame();
  } else {
    window.onload = startGame;
  }
}
castReceiverManager.start(appConfig);

