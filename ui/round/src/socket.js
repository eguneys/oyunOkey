import { game } from 'game';
import throttle from 'common/throttle';
import xhr from './xhr';
import ground from './ground';

export function make(send, ctrl) {

  function reload(o) {
    xhr.reload(ctrl).then(data => {
      if (oyunkeyf.socket.getVersion() > data.player.version) {
        oyunkeyf.reload();
      } else {
        ctrl.reload(data);
      }
    });
  }

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
      // ctrl.endWithData(scores);
    },
    endData(o) {
      ctrl.endWithData(o);
    },
    gone(o) {
      ['east', 'west', 'north', 'south'].forEach(function(side) {
        if (o[side]) {
          game.setIsGone(ctrl.data, side, o[side]);
          ctrl.redraw();
        }
      });
    },
    reload
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
