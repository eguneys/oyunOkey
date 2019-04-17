import m from 'mithril';

import { game } from 'game';

export default function(ctrl) {

  const d = ctrl.data.expiration;
  if (!d) return null;

  const timeLeft = Math.max(0, d.movedAt - Date.now() + d.millisToMove),
        secondsLeft = Math.floor(timeLeft / 1000),
        playerTurnPov = game.getTurnPov(ctrl.data),
        myTurn = playerTurnPov === 'player',
        emerg = myTurn && timeLeft < 8000;
        
  return [
    m('div.expiration.suggestion' + (emerg ? '.emerg':''), ctrl.trans('nbSecondsToPlayTheFirstMove', secondsLeft, m('strong', secondsLeft))),
    playerTurnPov
  ];

  
}
