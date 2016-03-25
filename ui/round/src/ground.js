import okeyground from 'okeyground';
import { game } from 'game';
import util from './util';

function makeFen(fen) {
  return util.fenStore.get(fen);
};

function makeConfig(data) {
  var fen = makeFen(data.game.fen);
  return {
    fen: fen,
    turnSide: data.game.player,
    povSide: data.player.side,
    movable: {
      free: false,
      board: game.isPlayerPlaying(data),
      dests: game.isPlayerPlaying(data) ? data.possibleMoves : []
    }
  };
}

function make(data, userMove, onMove) {
  var config = makeConfig(data);
  config.movable.events = {
    after: userMove
  };
  config.events = {
    move: onMove
  };
  return new okeyground.controller(config);
}

function reload(ground, data) {
  ground.set(makeConfig(data));
}

function end(ground) {
  ground.stop();
}

module.exports = {
  make: make,
  reload: reload,
  end: end
};
