import m from 'mithril';
import socket from './socket';
import ground from './ground';
import { game } from 'game';

module.exports = function(opts) {

  this.data = opts.data;

  this.vm = {
  };

  this.socket = new socket(opts.socketSend, this);

  var onUserMove = (key, piece) => {
    this.sendMove(key, piece);
  };

  var onMove = (key, piece) => {
    console.log('sound.move', key, piece);
  };

  this.okeyground = ground.make(this.data, onUserMove, onMove);


  this.sendMove = (key, piece) => {
    var move = {
      key,
      piece
    };

    this.socket.send('move', move, {
      ackable: true
    });
  };

  this.apiMove = (o) => {
    console.log(o);
    m.startComputation();
    var d = this.data,
        playing = game.isPlayerPlaying(d);
    d.game.turns = o.ply;
    d.game.player = game.sideByPly(o.ply);
    if (false) {
      // if (o.isMove) this.okeyground.apiMove();
      this.okeyground.set({
        turnSide: d.game.player,
        movable: {
          dests: playing ? {} : {}
        }
      });
    }
    console.log(d.game);
    m.endComputation();
  };
};
