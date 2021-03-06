"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.playable = playable;
exports.isPlayerPlaying = isPlayerPlaying;
exports.isPlayerTurn = isPlayerTurn;
exports.playedTurns = playedTurns;
exports.getTurnSide = getTurnSide;
exports.getTurnPov = getTurnPov;
exports.getPlayer = getPlayer;
exports.sideByPly = sideByPly;
exports.setOnGame = setOnGame;
exports.setIsGone = setIsGone;
exports.sides = void 0;

var _status = _interopRequireDefault(require("./status"));

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

function playable(data) {
  return data.game.status.id < _status["default"].ids.aborted;
}

function isPlayerPlaying(data) {
  return playable(data) && !data.player.spectator;
}

function isPlayerTurn(data) {
  return isPlayerPlaying(data) && data.game.player === data.player.side;
}

function playedTurns(data) {
  return data.game.turns;
}

function getTurnSide(data) {
  return sideByPly(data.game.turns);
}

function getTurnPov(data) {
  var turnSide = getTurnSide(data);
  return ['player', 'opponentLeft', 'opponentRight', 'opponentUp'].filter(function (k) {
    return data[k].side === turnSide;
  })[0];
}

function getPlayer(data, side) {
  return ['player', 'opponentLeft', 'opponentRight', 'opponentUp'].map(function (k) {
    return data[k];
  }).filter(function (player) {
    return player.side === side;
  })[0];
}

var sides = ["east", "north", "west", "south"];
exports.sides = sides;

function sideByPly(ply) {
  return sides[ply % 4];
}

function setOnGame(data, side, onGame) {
  var player = getPlayer(data, side);
  player.onGame = onGame;
}

function setIsGone(data, side, isGone) {
  var player = getPlayer(data, side);
  isGone = isGone && !player.ai;
  player.isGone = isGone;
}