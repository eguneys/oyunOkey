import m from 'mithril';
import { game } from 'game';
import util from './util';
import xhr from './xhr';
import ground from './ground';
import okeyground from 'okeyground';

const { partial } = okeyground.util;

module.exports = function(send, ctrl) {
  this.send = send;

  this.sendLoading = () => {
    ctrl.setLoading(true);
    this.send.apply(this, arguments);
  };

  var handlers = {
    move: function(o) {
      o.isMove = true;
      ctrl.apiMove(o);
    },
    crowd: function(o) {
      ['east', 'west', 'north', 'south'].forEach(function(side) {
        game.setOnGame(ctrl.data, side, o[side]);
      });
      m.redraw();
    },
    end: function(scores) {
      ctrl.data.game.scores = scores;
      ground.end(ctrl.okeyground);
      // ctrl.set loading?
      xhr.reload(ctrl).then(ctrl.reload);
    }
  };

  this.outoftime = util.throttle(1500, false, partial(this.send, 'outoftime', null));

  this.receive = (type, data) => {
    if (handlers[type]) {
      handlers[type](data);
      return true;
    }
    return false;
  };
};
