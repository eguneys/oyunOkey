var tab = {
  key: 'oyunkeyf.round.tab',
  fix: function(t) {
    if (['scores_tab', 'replay_tab'].indexOf(t) === -1) t = 'scores_tab';
    return t;
  }
};

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
      oyunkeyf.storage.set(conf.key, t);
      return t;
    },
    get: function() {
      return conf.fix(oyunkeyf.storage.get(conf.key));
    }
  };
}

module.exports = {
  tab: makeStore(tab),
  board: makeStore(board)
};
