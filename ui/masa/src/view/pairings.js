import m from 'mithril';
import { usernameOrAnon } from './util';

function round(ctrl, p) {
  return {
    tag: 'span',
    children: [ctrl.trans('roundX', p.r + 1)]
  };
}

function winner(ctrl, p) {
  if (p.s === 0) return null;
  var username = usernameOrAnon(ctrl, p.s);
  return {
    tag: p.s === 0 ? 'playing' : 'finished',
    children: [username]
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
