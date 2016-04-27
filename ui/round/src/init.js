import { game } from 'game';
import round from './round';
import title from './title';

module.exports = {
  startPly: function(data) {
    var lp = round.lastPly(data);
    var ls = round.lastStep(data);

    return [lp, Math.min(0, ls.moves.length - 1)];
  },
  yolo: function(ctrl) {
    var d = ctrl.data;

    title.init(ctrl);
    ctrl.setTitle();

    if (game.isPlayerPlaying(d)) {
      window.addEventListener('beforeunload', function(e) {
        if (game.playable(ctrl.data)) {
          ctrl.saveBoard();
          ctrl.socket.send('bye');
          var msg = ctrl.trans('thereIsAGameInProgress');
          (e || window.event).returnValue = msg;
          // return msg;
        }
      });
    }
  }
};
