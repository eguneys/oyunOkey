import m from 'mithril';
import button from './button';
import util from './util';

import okeyground from 'okeyground';
const { classSet, partial } = okeyground.util;

var scoreTagNames = ['penalty', 'erase'];

function scoreTag(s, i) {
  var tag = s > 0 ? 0 : 1;
  return {
    tag: scoreTagNames[tag],
    children: [s]
  };
}

function rank(p) {
  return m('rank', {}, [p.rank]);
}

function playerTr(ctrl, player) {
  var isLong = player.sheet.scores.length > 40;
  var playerName = player.name || 'Anonymous';
  var playerId = player.id;
  var userId = playerName.toLowerCase();
  return m('tr', {
    key: userId,
    class: classSet({
      'me': ctrl.playerId === playerId,
      'long': isLong
    })
  }, [
    m('td', [
      player.active ? rank(player) :
        m('rank', {
          'data-icon': 'b',
          'title': ctrl.trans('withdraw')
        }),
      util.player(player, 'span')
    ]),
    ctrl.data.isStarted ?
      m('td.sheet', player.sheet.scores.map(scoreTag)) : m('td'),
    ctrl.data.isStarted ?
      m('td.total', m('strong', player.sheet.total)) : null
  ]);
}

module.exports = {
  standing: function(ctrl, pag) {
    var tableBody = pag.currentPageResults ?
        pag.currentPageResults.map(partial(playerTr, ctrl)) :
        m.trust(oyunkeyf.spinnerHtml);

    return m('div.standing_wrap',
             m('table.slist.standing', [
               m('thead',
                 m('tr',
                   m('th.pager[colspan=3]', [
                     button.joinWithdraw(ctrl)
                   ])
                  )),
               m('tbody', {
               }, tableBody)
             ])
            );
  }
};
