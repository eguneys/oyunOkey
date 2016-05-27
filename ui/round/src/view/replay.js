import m from 'mithril';
import util from '../util';
import { game, status, uci as gameUci } from 'game';
import round from '../round';
import okeyground from 'okeyground';

const partial = okeyground.util.partial;
const raf = okeyground.util.requestAnimationFrame;

function renderMove(ctrl, step, ply, curPly) {
  var san = ctrl.trans.apply(null, gameUci.transKey(step.san));
  var children = [san];

  return {
    tag: 'move',
    attrs: (ply[0] !== curPly[0] || ply[1] !== curPly[1])
      ? {} : { class: 'active' },
    children: children
  };
}

function renderMoves(ctrl, turn) {

  var moves = turn.moves;

  var stepPly;
  var curPly;
  var rows = [];
  for (var i = 0, len = moves.length; i < len; i++) {
    stepPly = [turn.ply, i];
    rows.push(renderMove(ctrl, moves[i], stepPly, ctrl.vm.ply));
  }

  return rows;
}

function renderResult(ctrl) {
  var result;
  if (status.finished(ctrl.data)) switch(ctrl.data.game.winner) {
    default:
    result = ctrl.trans('gameEnded');
    break;
  }

  if (result || status.aborted(ctrl.data)) {
    var winner = game.getPlayer(ctrl.data, ctrl.data.game.winner);
    return [
      m('p.result', result),
      m('p.status', [
        //renderStatus(ctrl),
        winner ? ', ' + ctrl.trans('isVictorous'): null
      ])
    ];
  }
}

function renderTurns(ctrl) {
  var steps = ctrl.data.steps;

  var turns = steps;

  var player;
  var rows = [];
  for (var i = 0, len = turns.length; i < len; i++) {
    var turn = turns[i];
    var curPly = ctrl.vm.ply;

    player = game.getPlayer(ctrl.data, turn.side);
    player = player.user ? player.user.username :
      player.ai ? `Bot AI${player.ai}` : 'Anonymous';

    var plyTag = {
      tag: 'span',
      children: [turn.ply + 1]
    };

    var playerTag = {
      tag: 'span',
      attrs: { class: 'user' },
      children: [player]
    };

    rows.push({
      tag: 'turn',
      children: [{
        tag: 'index',
        attrs: (turn.ply !== curPly[0] || -1 !== curPly[1])
          ? {} : { class: 'active' },
        children: [plyTag, playerTag]
      },
                 m('div.moves', {},
                   renderMoves(ctrl, turn)
                  )]
    });
  }

  rows.push(renderResult(ctrl));

  return rows;
}

function autoScroll(el, ctrl) {
  raf(function() {
    if (round.replayLength(ctrl.data) < 7) return;
    var st;
    if (ctrl.vm.ply[0] >= round.lastPly(ctrl.data)) {
      st = 9999;
    } else {
      var plyEl = el.querySelector('.active') || el.querySelector('turn:first-child');
      if (plyEl) {
        var plyElParent = plyEl.parentElement;
        console.log(plyEl, plyElParent);

        st = plyEl.offsetTop - el.offsetHeight / 2 + plyEl.offsetHeight / 2;
        st += plyElParent.offsetTop;
      }
    }
    if (st !== undefined) el.scrollTop = st;
  });
}

module.exports = function(ctrl) {
  return m('div.replay', [
    m('div.turns', {
      config: function(el, isUpdate) {
        if (isUpdate) return;
        var scrollNow = partial(autoScroll, el, ctrl);
        ctrl.vm.autoScroll = {
          now: scrollNow,
          throttle: util.throttle(300, false, scrollNow)
        };
      }
    }, renderTurns(ctrl))
  ]);
};
