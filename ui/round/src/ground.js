import okeyground from 'okeyground';

function makeFen(fen) {
  return fen;
};

function makeConfig(data) {
  var fen = makeFen(data.game.fen);
  return {
    fen: fen,
    movable: {
      free: false
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
