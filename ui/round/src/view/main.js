import m from 'mithril';
import okeyground from 'okeyground';
import renderUser from './user';

function clockShowBar(ctrl, time) {
  return m('div', {
    class: 'bar'
  }, m('span', {
    style: {
      //width: Math.max(0, Math.min(100, (time / ctrl.data.barTime) * 100)) + '%'
      width: '90%'
    }
  }));
}

function renderClock(ctrl, side) {
  var time = 10;
  var running = true && ctrl.data.game.player === side;

  return running ? [
    m('div', {
      class: 'clock '
    }, [
      clockShowBar(ctrl.clock, time)
    ])
  ]: null;
}

function renderPlayer(ctrl, player) {
  return m('div', {
    class: 'player '
  },
           renderUser(ctrl, player),
           renderClock(ctrl, player.side)
   );
}

function renderSeat(ctrl, player) {
  return m('div', {
    class: 'table_seat'
  }, player ?
           renderPlayer(ctrl, player):
           m('span.empty_seat', {
           }, 'Empty'));
}

function visualTable(ctrl) {
  return m('div.oyunkeyf_table_wrap', [
    m('div', {
      class: 'oyunkeyf_table'
    }, okeyground.view(ctrl.okeyground))
  ]);
}

module.exports = function(ctrl) {
  var d = ctrl.data;
  return [
    m('div.top', [
      m('div', {
        class: 'oyunkeyf_game'
      }, [
        renderSeat(ctrl, d.opponentUp),
        m('div', {
          class: 'table_middle'
        }, [
          renderSeat(ctrl, d.opponentLeft),
          visualTable(ctrl),
          renderSeat(ctrl, d.opponentRight)
        ]),
        m('div.oyunkeyf_ground', [
        ]),
        renderSeat(ctrl, d.player)
      ])
    ])
  ];
};
