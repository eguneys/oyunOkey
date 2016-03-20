import okeyground from 'okeyground';
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
      dests: data.possibleMoves
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

module.exports = {
  make: make
};
