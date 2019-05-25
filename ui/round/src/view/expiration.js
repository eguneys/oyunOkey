import { h } from 'snabbdom';

import { game } from 'game';

export default function(ctrl, klass) {

  const d = game.playable(ctrl.data) && ctrl.data.expiration;
  if (!d) return null;

  const timeLeft = Math.max(0, d.movedAt - Date.now() + d.millisToMove),
        secondsLeft = Math.floor(timeLeft / 1000),
        myTurn = game.isPlayerTurn(ctrl.data),
        emerg = myTurn && timeLeft < 8000;

  const side = myTurn ? 'player' : 'opponent';
        
  return h('div.expiration.expiration-' + side, {
      class: {
        emerg,
        'bar-glider': myTurn
      }
  }, ctrl.trans.vdom('nbSecondsToPlayTheFirstMove', secondsLeft, h('strong', secondsLeft)));

}
