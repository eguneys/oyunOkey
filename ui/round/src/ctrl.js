import m from 'mithril';
import okeyground from 'okeyground';
import socket from './socket';
import round from './round';
import ground from './ground';
import title from './title';
import init from './init';
import clockCtrl from './clock/ctrl';
import mutil from './util';
import { game, status } from 'game';
import store from './store';

const { util } = okeyground;
const { wrapGroup, wrapPiece, wrapDrop, partial } = util;

module.exports = function(opts) {

  this.data = opts.data;

  // this.data.steps = [
  //   { ply: 16, side: 'south', moves: [{ san: 'Tas cekti' }] }
  // ];

  this.jump = (ply) => {
    this.vm.autoScroll && this.vm.autoScroll.throttle();
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

  this.vm = {
    // ply: [lastPly, lastStep.moves.length - 1];
    ply: init.startPly(this.data),
    tab: store.tab.get(),
    scoresheetInfo: {},
    autoScroll: null
  };

  this.setTab = (tab) => {
    this.vm.tab = store.tab.set(tab);
    this.vm.autoScroll && this.vm.autoScroll.now();
  };

  this.socket = new socket(opts.socketSend, this);

  this.setTitle = partial(title.set, this);

  var onUserMove = (key, move) => {
    this.sendMove(key, move);
  };

  var onMove = (key, piece) => {
    console.log('sound.move', key, piece);

    if (key === okeyground.move.drawMiddle) {
      this.sendMove(key);
    }

    if (key === okeyground.move.discard) {
      this.vm.hasPlayedDiscard = true;
    }
  };

  this.okeyground = ground.make(this.data, onUserMove, onMove);


  this.sendMove = (key, args = {}) => {
    var move = args;
    args.key = key;

    this.socket.send('move', move, {
      ackable: true
    });
  };

  this.leaveTaken = () => {
    this.sendMove(okeyground.move.leaveTaken);
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

  this.apiMove = (o) => {
    console.log(o);
    m.startComputation();
    var d = this.data,
        playing = game.isPlayerPlaying(d);

    var newTurn = o.ply !== round.lastPly(d);

    d.game.turns = o.ply;
    d.game.player = game.sideByPly(o.ply);

    d.possibleMoves = d.player.side === d.game.player ? o.dests : [];
    this.setTitle();
    if (true) {

      this.vm.ply[1]++;
      if (newTurn) this.vm.ply = [this.vm.ply[0] + 1, -1];

      if (o.isMove) {
        if (o.drawmiddle) {
          this.okeyground.apiMove(o.key, wrapPiece(o.drawmiddle.piece));
        } else if (o.discard) {
          if (!this.vm.hasPlayedDiscard) {
            this.okeyground.apiMove(o.key, wrapPiece(o.discard.piece));
          } else {
            // console.log('skip discard', o);
          }
          this.vm.hasPlayedDiscard = false;
        } else if (o.opens) {
          this.okeyground.apiMove(o.key, wrapGroup(o.opens.group));
        } else if (o.drop) {
          this.okeyground.apiMove(o.key, wrapDrop(o.drop.piece, o.drop.pos));
        } else if (o.key === okeyground.move.collectOpen) {
          // this.okeyground.apiMove(o.key, { fen: o.fen });
          var oldFen = this.okeyground.getFen();
          this.okeyground.set({
            fen: mutil.persistentFen(o.fen, oldFen)
          });
        } else if (o.key === okeyground.move.leaveTaken) {
          this.restoreFen(o.fen);
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

    if (o.clock) {
      //console.log('clock', [o.clock.east, o.clock.north, o.clock.west, o.clock.south].join('|'));
      var c = o.clock;
      if (this.clock) this.clock.update(c);
    }

    this.pushLastMove({
      uci: o.uci,
      san: o.uci
    });

    if (newTurn && status.playing(d)) {
      this.pushNewTurn();
    }

    console.log(d.game);
    m.endComputation();

    this.vm.autoScroll && this.vm.autoScroll.now();
  };

  this.reload = (cfg) => {
    m.startComputation();
    //this.vm.ply = round.lastStep(cfg).ply;
    this.vm.ply = round.lastVmPly(cfg);
    var merged = round.merge(this.data, cfg);
    this.data = merged.data;

    this.setTitle();
    // move on
    m.endComputation();
    this.vm.autoScroll && this.vm.autoScroll.now();
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

  this.clock = this.data.clock ? new clockCtrl(
    this.data.clock,
    this.socket.outoftime, this.data.player.side) : false;

  this.isClockRunning = () => {
    return this.data.clock && game.playable(this.data) &&
      ((this.data.game.turns > 0 ) || this.data.clock.running);
  };

  var clockTick = () => {
    if (this.isClockRunning()) this.clock.tick(this.data.game.player);
  };

  if (this.clock) setInterval(clockTick, 100);

  this.toggleScoresheet = (side, data) => {
    if (this.vm.scoresheetInfo.side === side) {
      side = null;
    }
    this.vm.scoresheetInfo = {
      side: side,
      data: data
    };
    m.redraw();
  };

  this.restoreFen = (fen) => {
    var oldFen = this.okeyground.getFen();
    this.okeyground.set({
      fen: mutil.persistentFen(fen, oldFen)
    });
  };

  this.saveBoard = () => {
    var boardFen = this.okeyground.getFen();
    mutil.fenStore.set(boardFen);
  };

  this.trans = oyunkeyf.trans(opts.i18n);

  init.yolo(this);
};
