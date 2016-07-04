import m from 'mithril';
import { usernameOrAnon, miniBoard } from './util';
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

function featured(f) {
  return m('div.featured', [
    miniBoard(f)
  ]);
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
    ctrl.data.featured ? featured(ctrl.data.featured) : null,
    m('div.box.all_pairings.scroll-shadow-soft', {
    }, ctrl.data.pairings.map(pairing))
  ];
}
