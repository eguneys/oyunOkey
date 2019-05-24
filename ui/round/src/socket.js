import { game } from 'game';
import throttle from 'common/throttle';
import xhr from './xhr';
import ground from './ground';

export function make(send, ctrl) {
  const handlers = {
    move(o) {
      o.isMove = true;
      var data = ctrl.data;
      ctrl.apiMove(o);
    },
    crowd(o) {
      ['east', 'west', 'north', 'south'].forEach(function(side) {
        game.setOnGame(ctrl.data, side, o[side]);
      });
      ctrl.redraw();
    },
    end(scores) {
      ctrl.data.game.scores = scores.result;
      ground.end(ctrl.okeyground);
      ctrl.saveBoard();
      ctrl.setLoading(true);
      xhr.reload(ctrl).then(ctrl.reload);
    },
    gone(o) {
      ['east', 'west', 'north', 'south'].forEach(function(side) {
        if (o[side]) {
          game.setIsGone(ctrl.data, side, o[side]);
          ctrl.redraw();
        }
      });
    }
  };

  return {
    send,

    sendLoading(typ, data) {
      ctrl.setLoading(true);
      send(typ, data);
    },
    outoftime: throttle(500, () => send('outoftime', null)),

    receive(type, data) {
      if (handlers[type]) {
        handlers[type](data);
        return true;
      }
      return false;
    }
  };
};
