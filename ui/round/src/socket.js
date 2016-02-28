module.exports = function(send, ctrl) {
  this.send = send;

  var handlers = {};

  this.receive = (type, data) => {
    if (handlers[type]) {
      handlers[type](data);
      return true;
    }
    return false;
  };
};
