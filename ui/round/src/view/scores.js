import m from 'mithril';
import okeyground from 'okeyground';
import { game } from 'game';
const { classSet, partial } = okeyground.util;


function normalizeScores(scores) {
  scores = Object.keys(scores).map(k => [scores[k], k]);
  scores.sort(([a, _], [b, __]) => { if (a === 4) return 1; else if (b === 4) return 1; return a - b;});
  return scores;
}

function playerTr(ctrl, player) {
  var isLong = player.scores.length > 5;
  var mySide = ctrl.data.player.side;

  function utilPlayer(p, tag) {
    var fullName = p.user ? p.user.username : 'Anonymous';
    var attrs = {
      class: 'user_link'
    };
    // if (p.name) attrs[tag === 'a' ? 'href' : 'data-href'] = '/@/' + p.name;
    return {
      tag: tag,
      attrs: attrs,
      children: fullName
    };
  }


  var scoreTagNames = ['penalty', 'erase', 'double', 'hand'];
  var scoreTagChild = ['+101', '-101', 'x2', `+${player.hand}`];
  function scoreTag(s, i) {
    return {
      tag: scoreTagNames[s - 1],
      children: [scoreTagChild[s - 1]]
    };
  }

  var scores = normalizeScores(player.scores).map(_ => _[0]);
  return m('tr', {
    key: player.side,
    class: classSet({
      'me': player.side === mySide,
      'long': isLong
    }),
    onclick: partial(ctrl.toggleScoresheet, player.side)
  }, [
    m('td', [
      utilPlayer(player, 'span')
    ]),
    m('td.sheet', scores.map(scoreTag)),
    m('td.total', m('strong', player.total))
  ]);
}

function renderTableScoreInfo(ctrl) {
  var d = ctrl.data;
  var side = ctrl.vm.scoresheetInfo.side;

  var player = d.game.scores[side];
  var name = (player.user ? player.user.username : 'Anonymous');

  var scores = normalizeScores(player.scores);
  var scoreTagNames = ['flag', 'penalty', 'erase', 'double', 'hand'];
  var scoreTagChild = ['-', '+101', '-101', 'x2', `+${player.hand}`];
  var flagNames = ['gameEndByHand', 'gameEndByPair', 'gameEndByDiscardOkey', 'handZero', 'handOkeyLeft', 'handNotOpened', 'handOpenedPair', 'handOpenedSome'].map(ctrl.trans);
  function scoreInfoTr(score) {
    var flag = score[1] - 1;
    var s = score[0];
    return m('tr', m('td', {
      tag: scoreTagNames[s],
      children: [scoreTagChild[s]]
    }), m('th', flagNames[flag]));
  }

  return m('div.scores_info', {
    onclick: partial(ctrl.toggleScoresheet, null)
  }, m('table',
       m('thead',
         m('tr', [
           m('td', player.total),
           m('th', name)
         ])
        ),
       m('tbody', scores.map(partial(scoreInfoTr)))));
}

function renderTableScores(ctrl) {
  var d = ctrl.data;

  if (!d.game.scores) return null;

  var scores = Object.keys(d.game.scores).map(k => {
    var s = d.game.scores[k];
    s.side = k;
    s.user = game.getPlayer(d, k).user;
    return s;
  });

  var tableBody = scores.map(partial(playerTr, ctrl));

  return m('div.scores', [
    m('p.top text', {
    }, ctrl.trans('scores')),
    m('table.slist.standing', [
      m('thead', m('tr')),
      m('tbody', tableBody)
    ])
  ]);
}

module.exports = {
  renderTableScores: renderTableScores,
  renderTableScoreInfo: renderTableScoreInfo
};
