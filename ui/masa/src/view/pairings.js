import m from 'mithril';

function round(ctrl, p) {
  return {
    tag: p.s === 0 ? 'playing' : 'finished',
    children: [ctrl.trans('roundX', p.r + 1)]
  };
}

module.exports = function(ctrl) {
  var pairing = function(p) {
    return {
      tag: 'a',
      attrs: {
        key: p.id,
        href: '/' + p.id
      },
      children: [
        round(ctrl, p)
      ]
    };
  };
  return [
    m('div.box.all_pairings.scroll-shadow-soft', {
    }, ctrl.data.pairings.map(pairing))
  ];
}
