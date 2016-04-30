import m from 'mithril';
import { game } from 'game';

function round(ctrl, p) {
  return {
    tag: p.s === 0 ? 'playing' : 'finished',
    children: [ctrl.trans('roundX', p.r + 1)]
  };
}

function winner(ctrl, p) {
  if (p.s === 0) return null;
  var player = game.getPlayer(ctrl.data, p.s);
  var name = player.user ? player.user.username : 'Anonymous';
  return {
    tag: 'div',
    children: [name]
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
        round(ctrl, p),
        winner(ctrl, p)
      ]
    };
  };
  return [
    m('div.box.all_pairings.scroll-shadow-soft', {
    }, ctrl.data.pairings.map(pairing))
  ];
}
