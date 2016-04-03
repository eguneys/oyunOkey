import m from 'mithril';
import okeyground from 'okeyground';
import socket from './socket';
import round from './round';
import ground from './ground';
import title from './title';
import init from './init';
import mutil from './util';
import { game } from 'game';

const { util } = okeyground;
const { wrapGroup, wrapPiece, wrapDrop, partial } = util;

module.exports = function(opts) {

  this.data = opts.data;

  this.vm = {
    scoresheetInfo: {
    }
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
    // this.sendMove(okeyground.move.leaveTaken);
  };

  this.collectOpen = () => {
    this.sendMove("co");
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
    d.game.turns = o.ply;
    d.game.player = game.sideByPly(o.ply);

    d.possibleMoves = d.player.side === d.game.player ? o.dests : [];
    this.setTitle();
    if (true) {
      if (o.isMove) {
        if (o.drawmiddle) {
          this.okeyground.apiDrawMiddleEnd(o.drawmiddle.piece);
        } else if (o.discard) {
          this.okeyground.apiMove(o.key, wrapPiece(o.discard.piece));
        } else if (o.opens) {
          this.okeyground.apiMove(o.key, wrapGroup(o.opens.group));
        } else if (o.drop) {
          this.okeyground.apiMove(o.key, wrapDrop(o.drop.piece, o.drop.pos));
        }else {
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
    console.log(d.game);
    m.endComputation();
  };

  this.reload = (cfg) => {
    m.startComputation();
    var merged = round.merge(this.data, cfg);
    this.data = merged.data;
    this.setTitle();
    // move on
    m.endComputation();
  };

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

  this.saveBoard = () => {
    var boardFen = this.okeyground.getFen();
    mutil.fenStore.set(boardFen);
  };

  this.trans = oyunkeyf.trans(opts.i18n);

  init.yolo(this);
};
