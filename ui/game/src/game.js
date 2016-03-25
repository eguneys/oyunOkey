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

function getPlayer(data, side) {
  return ['player', 'opponentLeft', 'opponentRight', 'opponentUp']
    .map(k => data[k])
    .filter(player => player.side === side)[0];
}

const sides = ["east", "north", "west", "south"];

function sideByPly(ply) {
  return sides[ply % 4];
}

module.exports = {
  isPlayerPlaying: isPlayerPlaying,
  isPlayerTurn: isPlayerTurn,
  getPlayer: getPlayer,
  sideByPly: sideByPly,
  playable: playable
};
