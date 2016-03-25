import m from 'mithril';
import socket from './socket';
import xhr from './xhr';
import { myCurrentGameId } from './masa';

module.exports = function(env) {
  this.data = env.data;
  this.userId = env.userId;

  this.playerId = env.data.playerId;

  this.socket = new socket(env.socketSend, this);

  this.vm ={
    joinSpinner: false
  };

  this.reload = (data) => {
    //if (this.data.isStarted !== data.isStarted) m.redraw.strategy('all');
    this.data = data;
    this.vm.joinSpinner = false;
    redirectToMyGame();
  };

  var redirectToMyGame = () => {
    var gameId = myCurrentGameId(this);
    if(gameId && oyunkeyf.storage.get('last-game') !== gameId)
      location.href = '/' + gameId;
  };

  this.join = (side) => {
    xhr.join(this, side);
    this.vm.joinSpinner = true;
  };

  this.withdraw = () => {
    xhr.withdraw(this);
    this.vm.joinSpinner = true;
  };

  redirectToMyGame();

  this.trans = oyunkeyf.trans(env.i18n);
};
