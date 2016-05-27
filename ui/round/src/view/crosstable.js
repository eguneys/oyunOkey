import m from 'mithril';
import okeyground from 'okeyground';
import { game } from 'game';
const { classSet, partial } = okeyground.util;
import { renderTableScoreInfo } from './scores';

function utilPlayer(p, tag) {
  var fullName = p.user ? p.user.username : (p.ai ? `Bot AI${p.ai}` : 'Anonymous');
  var attrs = {
    class: 'user_link'
  };
  if (p.user && p.user.username) attrs[tag === 'a' ? 'href' : 'data-href'] = '/@/' + p.user.username;
  return {
    tag: tag,
    attrs: attrs,
    children: fullName
  };
}

function normalizeScores(scores) {
  scores = Object.keys(scores).map(k => [scores[k], k]);
  scores.sort(([a, _], [b, __]) => { if (a === 4) return -1; else if (b === 4) return -1; return b - a;});
  return scores;
}

function arrayPad(arr, length, value) {
  var res = arr.slice(0);
  while (res.length < length) {
    res.unshift(value);
  }
  return res;
}

function compact(obj) {
  var res = {};
  for (var i in obj) {
    if (obj[i]) res[i] = obj[i];
  }
  return res;
}

function playerTr(ctrl, { player, scores, opens }) {
  var mySide = ctrl.data.player.side;

  var normalScores = normalizeScores(scores.scores).map(_ => _[0]);

  var opensHint = opens ? (opens.series ? 'openedSeries' : 'openedPairs'): null;

  return m('tr', {
    key: player.side,
    class: classSet({
      'me': player.side === mySide
    }),
    onclick: partial(ctrl.toggleScoresheet, player.side)
  }, [
    m('td.sheet', normalScores.map(partial(scoreTd, scores.hand))),
    m('th.score', scores.total),
    m('th.user', [
      utilPlayer(player, 'a'),
      opens ? (m('div.opens', {
        'data-hint': ctrl.trans(opensHint)
      }, opens.series ? opens.series : opens.pairs)) : null
    ])
  ]);
}

const emptyScoreTd = m('td', [{
  tag: 'a',
  children: m.trust('&nbsp;')
}]);

function scoreTd(hand, score) {
  var scoreTagNames = ['penalty', 'erase', 'double', 'hand'];
  var scoreTagChild = ['+101', '-101', 'x2', `+${hand}`];
  function scoreTag(s, i) {
    return {
      tag: scoreTagNames[s - 1],
      children: [scoreTagChild[s - 1]]
    };
  }
  return scoreTag(score);
}

module.exports = function(ctrl) {
  var d = ctrl.data;

  var sides = ['east', 'west', 'north', 'south'];

  var scores = sides.map(side => {
    return {
      player: game.getPlayer(d, side),
      scores: d.game.scores ? d.game.scores[side] : { scores: [] },
      opens: d.game.oscores ? d.game.oscores[side] : null
    };
  });

  var tableBody = scores.map(partial(playerTr, ctrl));

  return m('div.crosstable_wrap', [
    (ctrl.vm.scoresheetInfo.side && ctrl.data.game.scores) ? renderTableScoreInfo(ctrl) : m('div.scores_info'),
    m('div.crosstable',
      m('table', [
        m('thead', m('tr')),
        m('tbody', tableBody)
      ]))
  ]);
}
