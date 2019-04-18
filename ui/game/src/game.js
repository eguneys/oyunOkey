import status from './status';

function playable(data) {
  return data.game.status.id < status.ids.aborted;
}

function isPlayerPlaying(data) {
  return playable(data) && !data.player.spectator;
}

function isPlayerTurn(data) {
  return isPlayerPlaying(data) && data.game.player === data.player.side;
}

function getTurnSide(data) {
  return sideByPly(data.game.turns);  
}

function getTurnPov(data) {
  var turnSide = getTurnSide(data);
  return ['player', 'opponentLeft', 'opponentRight', 'opponentUp']
    .filter(k => data[k].side === turnSide)[0];
}

function getPlayer(data, side) {
  return ['player', 'opponentLeft', 'opponentRight', 'opponentUp']
    .map(k => data[k])
    .filter(player => player.side === side)[0];
}

const sides = ["east", "north", "west", "south"];

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

module.exports = {
  isPlayerPlaying: isPlayerPlaying,
  isPlayerTurn: isPlayerTurn,
  getPlayer: getPlayer,
  sideByPly: sideByPly,
  playable: playable,
  setOnGame: setOnGame,
  setIsGone: setIsGone,
  getTurnPov: getTurnPov
};
