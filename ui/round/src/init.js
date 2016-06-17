import m from 'mithril';
import { game } from 'game';
import round from './round';
import title from './title';

module.exports = {
  startPly: function(data) {
    var lp = round.lastPly(data);
    var ls = round.lastStep(data);
    return [lp, ls.moves.length - 1];
  },
  yolo: function(ctrl) {
    var d = ctrl.data;

    title.init(ctrl);
    ctrl.setTitle();

    if (game.isPlayerPlaying(d)) {
      window.addEventListener('beforeunload', function(e) {
        if (!oyunkeyf.hasToReload && game.playable(ctrl.data) && ctrl.data.clock) {
          ctrl.saveBoard();
          ctrl.socket.send('bye');
          var msg = ctrl.trans('thereIsAGameInProgress');
          (e || window.event).returnValue = msg;
          // return msg;
        }
      });
    }

    if (!ctrl.data.player.spectator &&
        ctrl.vm.ply[0] === round.lastPly(ctrl.data)) {
      setTimeout(function() {
        if (ctrl.jump(round.lastPly(ctrl.data))) m.redraw();
      }, 200);
    }
  }
};
