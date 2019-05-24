import { h } from 'snabbdom';
import Okeyground from 'okeyground';
import * as util from './util';

function makeFen(fen) {
  return util.fenStore.get(fen);
};

function makeConfig(ctrl) {
  const data = ctrl.data, hooks = ctrl.makeOgHooks();
  var fen = makeFen(data.game.fen);

  var isPlaying = ctrl.isPlaying();

  return {
    fen: fen,
    turnSide: data.game.player,
    povSide: data.player.side,
    spectator: data.player.spectator,
    withTore: !!data.game.variant.key.match(/duzokey/),
    movable: {
      free: false,
      board: isPlaying,
      dests: isPlaying ? data.possibleMoves : [],
      events: {
        after: hooks.onUserMove
      }
    },
    events: {
      move: hooks.onMove
    }
  };
}

export function make(data, userMove, onMove) {
  var config = makeConfig(data);
  config.movable.events = {
    after: userMove
  };
  config.events = {
    move: onMove
  };
  return new okeyground.controller(config);
}

export function reload(ground, data) {
  ground.set(makeConfig(data));
}

export function end(ground) {
  ground.stop();
}

export function render(ctrl) {
  return h('div.cg-wrap', {
    hook: util.onInsert(el => ctrl.setOkeyground(Okeyground(el, makeConfig(ctrl))))
  });
}
