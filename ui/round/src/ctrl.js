import okeyground from 'okeyground';
import { make as makeSocket } from './socket';
import round from './round';
import ground from './ground';
import * as title from './title';
import init from './init';
import { ClockController } from './clock/ctrl';
import mutil from './util';
import { game, status } from 'game';
import store from './store';

const { util } = okeyground;
const { wrapGroup, wrapPiece, wrapDrop, partial } = util;

module.exports = function(opts, redraw) {

  // will be replaced by view layer
  this.autoScroll = $.noop;

  round.massage(opts.data);
  
  const d = this.data = opts.data;

  this.opts = opts;
  this.redraw = redraw;

  this.ply = init.startPly(this.data);
  // this.data.steps = [
  //   { ply: 16, side: 'south', moves: [{ san: 'Tas cekti' }] }
  // ];

  this.socket = makeSocket(opts.socketSend, this);

  this.trans = oyunkeyf.trans(opts.i18n);

  this.makeOgHooks = () => ({
    onUserMove: onUserMove,
    onMove: onMove
  });

  this.isPlaying = () => game.isPlayerPlaying(this.data);

  this.jump = (ply) => {
    this.autoScroll();
    return true;
  };

  this.pushNewTurn = () => {
    this.data.steps.push({
      ply: round.lastPly(this.data) + 1,
      moves: [], side: game.sideByPly(this.data.game.turns) });
  };

  var newTurn = this.data.game.turns !== round.lastPly(this.data);

  if (newTurn && status.playing(this.data)) {
    this.pushNewTurn();
  }

  this.pushLastMove = (move, newTurn) => {
    var lastTurn = this.data.steps.splice(-1, 1)[0];
    lastTurn.moves.push(move);
    this.data.steps.push(lastTurn);
  };

  this.setTab = (tab) => {
    this.vm.tab = store.tab.set(tab);
    this.autoScroll();
  };

  this.showExpiration = () => {
    if (!this.data.expiration) return;
    this.redraw();
    setTimeout(this.showExpiration, 250);
  };
  
  var onUserMove = (key, move) => {
    if (key === okeyground.move.leaveTaken) {
      return;
    }

    this.sendMove(key, move);
  };

  var onMove = (key, piece) => {
    console.log('sound.move', key, piece);

    if (key === okeyground.move.drawMiddle) {
      this.sendMove(key);
    }

    if (key === okeyground.move.discard) {
      this.hasPlayedDiscard = true;
    }
  };

  // this.okeyground = ground.make(this.data, onUserMove, onMove);


  this.sendMove = (key, args = {}) => {
    var move = args;
    args.key = key;

    this.socket.send('move', move, {
      ackable: true
    });
  };

  this.leaveTaken = () => {
    this.sendMove(okeyground.move.leaveTaken);
    // this.okeyground.playLeaveTaken();
  };

  this.collectOpen = () => {
    this.sendMove(okeyground.move.collectOpen);
  };

  this.openSeries = () => {
    this.okeyground.playOpenSeries();
  };

  this.openPairs = () => {
    this.okeyground.playOpenPairs();
  };

  this.sortPairs = () => {
    this.okeyground.sortPairs();
  };

  this.sortSeries = () => {
    this.okeyground.sortSeries();
  };

  this.canCollectOpen = () => {
    return this.okeyground && this.okeyground.canCollectOpen();
  };
  this.canLeaveTaken = () => {
    return this.okeyground && this.okeyground.canLeaveTaken();
  };
  this.canOpenSeries = () => {
    return this.okeyground && this.okeyground.canOpenSeries();
  };
  this.canOpenPairs = () => {
    return this.okeyground && this.okeyground.canOpenPairs();
  };

  this.apiMove = (o) => {
    console.log('api move', o);
    var d = this.data,
        playing = game.isPlayerPlaying(d);

    var newTurn = o.ply !== round.lastPly(d);

    d.game.turns = o.ply;
    d.game.player = game.sideByPly(o.ply);
    d.game.oscores = o.oscores;

    d.possibleMoves = d.player.side === d.game.player ? o.dests : [];
    this.setTitle();
    if (true) {

      this.ply[1]++;
      if (newTurn) this.ply = [this.ply[0] + 1, -1];

      if (o.isMove) {
        if (o.drawmiddle) {
          this.okeyground.apiMove(o.key, wrapPiece(o.drawmiddle.piece));
        } else if (o.discard) {
          if (!this.hasPlayedDiscard) {
            this.okeyground.apiMove(o.key, wrapPiece(o.discard.piece));
          } else {
            // console.log('skip discard', o);
          }
          this.hasPlayedDiscard = false;
        } else if (o.opens) {
          this.okeyground.apiMove(o.key, wrapGroup(o.opens.group));
        } else if (o.drop) {
          this.okeyground.apiMove(o.key, wrapDrop(o.drop.piece, o.drop.pos));
        } else if (o.key === okeyground.move.collectOpen) {
          this.restoreFen(o.fen, okeyground.move.collectOpen);
        } else if (o.key === okeyground.move.leaveTaken) {
          this.okeyground.apiMove(o.key, wrapPiece(o.leavetaken.piece));
        } else {
          this.okeyground.apiMove(o.key);
        }
      }

      this.okeyground.set({
        turnSide: d.game.player,
        movable: {
          dests: playing ? d.possibleMoves : []
        }
      });
    }

    this.pushLastMove({
      uci: o.uci,
      san: o.uci
    });

    if (newTurn && status.playing(d)) {
      this.pushNewTurn();
    }

    if (o.clock) {
      const oc = o.clock;
      if (this.clock) this.clock.setClock(d, oc.east,
                                          oc.west,
                                          oc.north,
                                          oc.south);
    }

    if (this.data.expiration) {
      if (this.data.steps.length > 4) this.data.expiration = undefined;
      else this.data.expiration.movedAt = Date.now();
    }

    this.redraw();
    this.autoScroll();
  };

  this.reload = (cfg) => {

    round.massage(cfg);
    //this.vm.ply = round.lastStep(cfg).ply;
    this.ply = round.lastVmPly(cfg);
    var merged = round.merge(this.data, cfg);
    this.data = merged.data;
    if (this.clock) this.clock.setClock(d,
                                        d.clock.east,
                                        d.clock.west,
                                        d.clock.north,
                                        d.clock.south);
    this.setTitle();
    // move on
    this.autoScroll();
    this.setLoading(false);
  };

  // this.data.clock = {
  //   running: true,
  //   initial: 60,
  //   emerg: 10,
  //   sides: {
  //     east: 10,
  //     west: 60,
  //     north: 60,
  //     south: 60
  //   }
  // };

  this.clock = this.data.clock ? new ClockController(
    this.data, {
      onFlag: this.socket.outoftime,
      soundSide: this.data.player.side
    }) : false;

  this.toggleScoresheet = (side, data) => {
    if (this.vm.scoresheetInfo.side === side) {
      side = null;
    }
    this.vm.scoresheetInfo = {
      side: side,
      data: data
    };
    this.redraw();
  };

  this.restoreFen = (fen, hint) => {
    var oldBoard = this.okeyground.getFen();

    // make a hack fen to split
    var oldFen = "//" + oldBoard + "/";

    this.okeyground.set({
      fen: mutil.persistentFen(fen, oldFen),
      animationHint: hint
    });
  };

  this.saveBoard = () => {
    var boardFen = this.okeyground.getFen();
    mutil.fenStore.set(boardFen);
  };

  this.setLoading = (v) => {
    clearTimeout(this.vm.loadingTimeout);
    if (v) {
      this.vm.loading = true;
      this.vm.loadingTimeout = setTimeout(() => {
        this.vm.loading = false;
        this.redraw();
      }, 1500);
    } else {
      this.vm.loading = false;
    }
    this.redraw();
  };

  this.setTitle = () => title.set(this);

  this.setOkeyground = (og) => {
    this.okeyground = og;
  };

  this.delayedInit = () => {
    const d = this.data;
    
    title.init();
    this.setTitle();
    window.addEventListener('beforeunload', function(e) {
      if (!oyunkeyf.hasToReload && game.playable(d) && d.clock) {
        this.saveBoard();
        this.socket.send('bye');
        var msg = ctrl.trans('thereIsAGameInProgress');
        (e || window.event).returnValue = msg;
        // return msg;
      }
    });
  };


  setTimeout(this.delayedInit, 200);
  setTimeout(this.showExpiration, 350);
};
