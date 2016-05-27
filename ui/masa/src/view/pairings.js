import m from 'mithril';
import { usernameOrAnon } from './util';
import { statusIds } from '../masa';

function round(ctrl, p) {
  var tag = (p.s <= statusIds.middleEnd && p.s > statusIds.started) ? 'aborted' : 'span';
  return {
    tag: tag,
    children: [ctrl.trans('roundX', p.r + 1)]
  };
}

function winner(ctrl, p) {
  if (!p.w) return null;
  var username = usernameOrAnon(ctrl, p.w);
  return {
    tag: p.s < statusIds.aborted ? 'playing' : 'finished',
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
