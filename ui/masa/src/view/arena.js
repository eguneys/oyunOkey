import { h } from 'snabbdom';
import * as button from './button';
import { player as renderPlayer, bind } from './util';

import okeyground from 'okeyground';
const { classSet, partial } = okeyground.util;

var scoreTagNames = ['penalty', 'erase'];

function scoreTag(s, i) {
  var tag = s > 0 ? 0 : 1;
  return h(scoreTagNames[tag], [s]);
}

function playerTr(ctrl, player) {
  var isLong = player.sheet.scores.length > 35;

  var ai = ctrl.data.players[player.id].ai;
  var active = ctrl.data.players[player.id].active;

  var playerName = ai ? ctrl.trans('aiBot', ai) :
      (player.name || 'Misafir');
  playerName = active ? playerName : ctrl.trans('emptySeat');

  var inviteButton = active ? playerName :
      [playerName, button.invite(ctrl)];

  var playerId = player.id;
  var userId = playerName.toLowerCase();

  var scores = ctrl.data.scores ? ctrl.data.scores : 0;
  // var scoreTotal = player.sheet.total + scores;
  var scoreTotal = player.score;

  return h('tr', {
    key: playerId,
    class: {
      me: ctrl.playerId === playerId,
      long: isLong
    }
  }, [
    h('td.rank',
      player.active ? player.rank :
        h('i', {
          attrs: {
            'data-icon': 'b',
            'title': ctrl.trans('withdraw')
          }
        })
     ),
    h('td.player', player.active && player.name ? renderPlayer(player): inviteButton),
    h('td.sheet', player.sheet.scores.map(scoreTag)),
    h('td.total', h('strong', scoreTotal))
  ]);
}

function podiumUsername(p) {
  return p.name ? h('a.text.ulpt.user-link', {
    attrs: { href: '/@/' + p.name }
  }, p.name) : 'Anonymous';
}

function podiumStats(p, data) {
  var ratingDiff;
  if (p.ratingDiff === 0) ratingDiff = h('span', ' =');
  else if (p.ratingDiff > 0) ratingDiff = h('span.positive[data-icon=N]', p.ratingDiff);
  else if (p.ratingDiff < 0) ratingDiff = h('span.negative[data-icon=M]', -p.ratingDiff);

  var nb = p.nb;
  return [
    p.rating ? h('span.rating.progress', [
      p.rating + p.ratingDiff,
      ratingDiff]) : null,
    h('table.stats', [])
  ];
}

function podiumPosition(p, pos, trans) {
  if (p) return h('div.' + pos, [
    h('div.trophy'),
    p.ai ? trans('aiBot', p.ai) : podiumUsername(p),
    podiumStats(p, trans)
  ]);
}

export function podium(ctrl) {
  const p = ctrl.data.podium || [];

  return h('div.masa__podium', [
    podiumPosition(p[0], 'first', ctrl.trans)
  ]);
}

export function controls(ctrl, pag) {
  return h('div.masa__controls', [
    button.joinWithdraw(ctrl)
  ]);
}

var lastBody;

export function standing(ctrl, pag) {
  const tableBody = pag.currentPageResults ?
        pag.currentPageResults.map(res => playerTr(ctrl, res)) : lastBody;

  if (pag.currentPageResults) lastBody = tableBody;

  var playersToStart = 4 - ctrl.data.nbPlayers;
  var canJoinOrWithdraw = playersToStart > 0 ||
      (ctrl.data.me && ctrl.data.me.active);

  return h('table.slist.masa__standing', [
    h('tbody', {
    }, tableBody)
  ]);
}
