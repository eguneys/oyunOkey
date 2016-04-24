import { game, status } from 'game';

const initialTitle = document.title;

var init = function(ctrl) {
};

var set = function(ctrl, text) {
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

module.exports = {
  set: set,
  init: init
};
