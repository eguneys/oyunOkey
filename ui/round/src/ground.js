import okeyground from 'okeyground';

function makeFen(fen) {
  return fen;
};

function makeConfig(data) {
  var fen = makeFen(data.game.fen);
  return {
    fen: fen
  };
}

function make(data) {
  var config = makeConfig(data);
  return new okeyground.controller(config);
}

module.exports = {
  make: make
};
