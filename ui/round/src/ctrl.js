import m from 'mithril';
import okeyground from 'okeyground';
import socket from './socket';
import ground from './ground';
import util from './util';
import { game } from 'game';

const { util: { partial } } = okeyground;

module.exports = function(opts) {

  this.data = opts.data;

  this.vm = {
  };

  this.socket = new socket(opts.socketSend, this);

  var onUserMove = (key, piece, group) => {
    this.sendMove(key, piece, group);
  };

  var onMove = (key, piece) => {
    console.log('sound.move', key, piece);

    if (key === okeyground.move.drawMiddle) {
      this.sendMove(key);
    }
  };

  this.okeyground = ground.make(this.data, onUserMove, onMove);


  this.sendMove = (key, piece, group) => {
    var move = {
      key,
      piece,
      group
    };

    this.socket.send('move', move, {
      ackable: true
    });
  };

  this.leaveTaken = () => {
    // this.sendMove(okeyground.move.leaveTaken);
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

    if (true) {
      if (o.isMove) {
        if (o.drawmiddle) {
          this.okeyground.apiDrawMiddleEnd(o.drawmiddle.piece);
        } else {
          if (o.discard) {
            this.okeyground.apiMove(o.key, o.discard.piece);
          } else {
            this.okeyground.apiMove(o.key);
          }
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

  this.saveBoard = () => {
    var boardFen = this.okeyground.getFen();
    util.fenStore.set(boardFen);
  };
};
