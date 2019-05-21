const tab = {
  key: 'lobby.tab',
  fix(t) {
    if (t) return t;
    return 'pools';
  }
};

function makeStore(conf, userId) {
  const fullKey = conf.key + ':' + (userId || '-');
  return {
    set(v) {
      const t = conf.fix(v);
      window.oyunkeyf.storage.set(fullKey, '' + t);
      return t;
    },
    get() {
      return conf.fix(window.oyunkeyf.storage.get(fullKey));
    }
  };
}

export function make(userId) {
  return {
    tab: makeStore(tab, userId)
  };
}
