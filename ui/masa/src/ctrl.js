import socket from './socket';

module.exports = function(env) {
  this.data = env.data;
  this.userId = env.userId;

  this.socket = new socket(env.socketSend, this);

  this.vm ={
    joinSpinner: false
  };

  this.reload = function(data) {
    console.log(data);
  };
};
