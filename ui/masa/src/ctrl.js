import socket from './socket';
import xhr from './xhr';

module.exports = function(env) {
  this.data = env.data;
  this.userId = env.userId;

  this.socket = new socket(env.socketSend, this);

  this.vm ={
    joinSpinner: false
  };

  this.reload = (data) => {
    this.data = data;
    this.vm.joinSpinner = false;
  };

  this.join = (side) => {
    xhr.join(this, side);
    this.vm.joinSpinner = true;
  };

  this.withdraw = () => {
    xhr.withdraw(this);
    this.vm.joinSpinner = true;
  };
};
