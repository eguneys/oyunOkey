import status from './status';

export function playable(data) {
  return data.game.status.id < status.ids.aborted;
}

export function isPlayerPlaying(data) {
  return playable(data) && !data.player.spectator;
}

export function isPlayerTurn(data) {
  return isPlayerPlaying(data) && data.game.player === data.player.side;
}

export function playedTurns(data) {
  return data.game.turns;
}

export function getTurnSide(data) {
  return sideByPly(data.game.turns);  
}

export function getTurnPov(data) {
  var turnSide = getTurnSide(data);
  return ['player', 'opponentLeft', 'opponentRight', 'opponentUp']
    .filter(k => data[k].side === turnSide)[0];
}

export function getPlayer(data, side) {
  return ['player', 'opponentLeft', 'opponentRight', 'opponentUp']
    .map(k => data[k])
    .filter(player => player.side === side)[0];
}

export const sides = ["east", "north", "west", "south"];

export function sideByPly(ply) {
  return sides[ply % 4];
}

export function setOnGame(data, side, onGame) {
  var player = getPlayer(data, side);
  player.onGame = onGame;
}

export function setIsGone(data, side, isGone) {
  var player = getPlayer(data, side);
  isGone = isGone && !player.ai;
  player.isGone = isGone;
}
