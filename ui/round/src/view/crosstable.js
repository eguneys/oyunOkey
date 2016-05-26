import m from 'mithril';
import okeyground from 'okeyground';
import { game } from 'game';
const { classSet, partial } = okeyground.util;
import { renderTableScoreInfo } from './scores';

function utilPlayer(p, tag) {
  var fullName = p.user ? p.user.username : 'Anonymous';
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

function playerTr(ctrl, player) {
  var isLong = player.scores.length > 5;
  var mySide = ctrl.data.player.side;

  var scores = normalizeScores(player.scores).map(_ => _[0]);

  return m('tr', {
    key: player.side,
    class: classSet({
      'me': player.side === mySide,
      'long': isLong
    }),
    onclick: partial(ctrl.toggleScoresheet, player.side)
  }, [
    m('td.sheet', scores.map(partial(scoreTd, player.hand))),
    m('th.score', player.total),
    m('th.user', utilPlayer(player, 'a'))
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

  var gameScores = d.game.scores || {};

  var scores = Object.keys(gameScores).map(k => {
    var s = d.game.scores[k];
    s.side = k;
    s.user = game.getPlayer(d, k).user;
    return s;
  });
  var tableBody = scores.map(partial(playerTr, ctrl));

  return m('div.crosstable_wrap', [
    ctrl.vm.scoresheetInfo.side ? renderTableScoreInfo(ctrl) : m('div.scores_info'),
    m('div.crosstable',
      m('table', [
        m('thead', m('tr')),
        m('tbody', tableBody)
      ]))
  ]);
}
