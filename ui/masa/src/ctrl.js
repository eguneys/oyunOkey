import makeSocket from './socket';
import xhr from './xhr';

export default function MasaController(opts, redraw) {

  this.askReload = () => {
    if (this.joinSpinner) xhr.reloadNow(this);
    else xhr.reloadSoon(this);
  };

  this.reload = (data) => {
    this.data = data;
    this.playerId = data.playerId;
    this.seatId = data.seatId;
    this.loadPage(data.standing);
    this.joinSpinner = false;
    redirectToMyGame();
  };

  this.lastStorage = oyunkeyf.storage.make('last-redirect');

  this.myGameId = () => this.data.me && this.data.me.gameId;

  var redirectToMyGame = () => {
    var gameId = this.myGameId();
    if (gameId) redirectFirst(gameId);
  };

  var redirectFirst = (gameId) => {
    const delay = (1000 + Math.random() * 500);
    setTimeout(() => {
      if (this.lastStorage.get() !== gameId) {
        this.lastStorage.set(gameId);
        oyunkeyf.redirect('/' + gameId);
      }
    }, delay);
  };

  this.loadPage = (data) => {
    this.pages[data.page] = data.players;
  };

  this.invite = (side) => {
    xhr.invite(this, side);
    this.joinSpinner = true;
  };

  this.join = (side) => {
    xhr.join(this, side);
    this.joinSpinner = true;
  };

  this.withdraw = () => {
    xhr.withdraw(this);
    this.joinSpinner = true;
  };

  this.pages = {};

  this.opts = opts;
  this.data = opts.data;
  this.redraw = redraw;

  this.userId = opts.userId;
  this.playerId = opts.data.playerId;
  this.seatId = opts.data.seatId;

  this.trans = oyunkeyf.trans(opts.i18n);

  this.socket = makeSocket(opts.socketSend, this);

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
