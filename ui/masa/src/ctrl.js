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
    pages: {},
    joinSpinner: false
  };

  this.reload = (data) => {
    //if (this.data.isStarted !== data.isStarted) m.redraw.strategy('all');
    this.data = data;
    this.playerId = data.playerId;
    this.loadPage(data.standing);
    this.vm.joinSpinner = false;
    redirectToMyGame();
  };

  var redirectToMyGame = () => {
    var gameId = myCurrentGameId(this);
    if(gameId && oyunkeyf.storage.get('last-game') !== gameId)
      location.href = '/' + gameId;
  };

  this.loadPage = (data) => {
    this.vm.pages[data.page] = data.players;
  };
  this.loadPage(this.data.standing);
  // this.loadPage({ page: 1, players: [{
  //   rank: 1,
  //   score: 1000,
  //   sheet: {
  //     scores: [101, 404, 303, 0, -10, 101],
  //     total : 1000
  //   }
  // }, {
  //   rank: 2,
  //   name: "lkajsdflksajlfkjsalkfd",
  //   score: 1000,
  //   sheet: {
  //     scores: [-101, 404, 0, 303, 101],
  //     total : 1000
  //   }
  // }, {
  //   rank: 3,
  //   score: 1000,
  //   sheet: {
  //     scores: [101, 404, 303, -200, 0, 101],
  //     total : 1000
  //   }
  // }] });

  this.invite = (side) => {
    xhr.invite(this, side);
    this.vm.joinSpinner = true;
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
