import okeyground from 'okeyground';

function makeFen(boardFen, tableFen) {
  return [boardFen, tableFen].join('/');
};

function makeConfig(data) {
  var fen = makeFen('r1r2r3', 'r2r3/r8l8/r1r1 r2r2/20l1');
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
