var board = {
  key: 'oyunkeyf.round.board',
  fix: function(b) {
    return b;
  }
};

function makeStore(conf) {
  return {
    set: function(t) {
      t = conf.fix(t);
      oyunkeyf.storage(conf.key, t);
      return t;
    },
    get: function() {
      return conf.fix(oyunkeyf.storage(conf.key));
    }
  };
}

module.exports = {
  board: makeStore(board)
};
