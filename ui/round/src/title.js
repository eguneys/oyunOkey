import { game, status } from 'game';

const initialTitle = document.title;

export function init(ctrl) {
};

export function set(ctrl, text) {
  if (ctrl.data.player.spectator) return;
  if (!text) {
    if (status.finished(ctrl.data)) {
      text = ctrl.trans('gameOver');
    } else if (game.isPlayerTurn(ctrl.data)) {
      text = ctrl.trans('yourTurn');
    } else {
      text = ctrl.trans('waitingForOpponent');
    }
  }
  document.title = text + " - " + initialTitle;
};
