module.exports = function(send, ctrl) {
  this.send = send;

  var handlers = {
    move: function(o) {
      o.isMove = true;
      ctrl.apiMove(o);
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
