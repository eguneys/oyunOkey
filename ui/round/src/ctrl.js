import socket from './socket';
import ground from './ground';

module.exports = function(opts) {

  this.data = opts.data;

  this.vm = {
  };

  this.socket = new socket(opts.socketSend, this);

  this.okeyground = ground.make(this.data);

};
