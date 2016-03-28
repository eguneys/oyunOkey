import xhr from './xhr';
import ground from './ground';

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
    end: function(scores) {
      ctrl.data.game.scores = scores;
      ground.end(ctrl.okeyground);
      // ctrl.set loading?
      xhr.reload(ctrl).then(ctrl.reload);
    }
  };

  this.receive = (type, data) => {
    if (handlers[type]) {
      handlers[type](data);
      return true;
    }
    return false;
  };
};
