import makeSocket from './socket';
import xhr from './xhr';
import { myCurrentGameId } from './masa';

export default function MasaController(opts, redraw) {
  this.pages = {};

  this.opts = opts;
  this.data = opts.data;
  this.redraw = redraw;

  this.userId = opts.userId;
  this.playerId = opts.data.playerId;
  this.seatId = opts.data.seatId;

  this.trans = oyunkeyf.trans(opts.i18n);

  this.socket = makeSocket(opts.socketSend, this);

  this.reload = (data) => {
    this.data = data;
    this.playerId = data.playerId;
    this.seatId = data.seatId;
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
    this.pages[data.page] = data.players;
  };

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

  redirectToMyGame();

};
