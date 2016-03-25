import { game, status } from 'game';

const initialTitle = document.title;

var init = function(ctrl) {
};

var set = function(ctrl, text) {
  if (!text) {
    if (status.finished(ctrl.data)) {
      text = 'transGameOver';
    } else if (game.isPlayerTurn(ctrl.data)) {
      text = 'transYourTurn';
    } else {
      text = 'transWaitingForOpponent';
    }
  }
  document.title = text + " - " + initialTitle;
};

module.exports = {
  set: set,
  init: init
};
