import m from 'mithril';
import util from '../util';
import { game } from 'game';
import round from '../round';
import okeyground from 'okeyground';

const partial = okeyground.util.partial;
const raf = okeyground.util.requestAnimationFrame;

function renderMove(step, ply, curPly) {
  var children = [step.san];

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
    rows.push(renderMove(moves[i], stepPly, ctrl.vm.ply));
  }

  return rows;
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
    player = player.user ? player.user.username : 'Anonymous';

    rows.push({
      tag: 'turn',
      children: [{
        tag: 'index',
        attrs: (turn.ply !== curPly[0] || -1 !== curPly[1])
          ? {} : { class: 'active' },
        children: [player]
      },
                 m('div.moves', {},
                   renderMoves(ctrl, turn)
                  )]
    });
  }

  return rows;
}

function autoScroll(el, ctrl) {
  raf(function() {
    if (round.replayLength(ctrl.data) < 7) return;
    var st;
    {
      var plyEl = el.querySelector('.active') || el.querySelector('turn:first-child');
      if (plyEl) {
        var plyElParent = plyEl.parentElement;

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
