import xhr from './xhr';

module.exports = function(send, ctrl) {
  this.send = send;

  var handlers = {
    reload: function() {
      xhr.reloadMasa(ctrl);
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
