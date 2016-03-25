import { game } from 'game';
import title from './title';

module.exports = {
  yolo: function(ctrl) {
    var d = ctrl.data;

    title.init(ctrl);
    ctrl.setTitle();

    if (game.isPlayerPlaying(d)) {
      window.addEventListener('beforeunload', function(e) {
        if (game.playable(ctrl.data)) {
          ctrl.socket.send('bye');
          var msg = 'trans There is a game in progress!';
          (e || window.event).returnValue = msg;
          // return msg;
        }
      });
    }
  }
};
