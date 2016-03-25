import m from 'mithril';
import okeyground from 'okeyground';
import renderUser from './user';
import { game } from 'game';
import button from './button';

function compact(x) {
  if (Object.prototype.toString.call(x) === '[object Array]') {
    var elems = x.filter(function(n) {
      return n !== undefined;
    });
    return elems.length > 0 ? elems : null;
  }
  return x;
}

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

function renderClock(ctrl, side, position) {
  var time = 10;
  var running = true && ctrl.data.game.player === side;

  return running ? [
    m('div', {
      class: 'clock clock_' + side + ' clock_' + position
    }, [
      clockShowBar(ctrl.clock, time)
    ])
  ]: null;
}

function isSpinning(ctrl) {
  return ctrl.vm.loading || ctrl.vm.redirecting;
}

function spinning(ctrl) {
  if (isSpinning(ctrl)) return m.trust(oyunkeyf.spinnerHtml);
}

function renderTableEnd(ctrl) {
  var d = ctrl.data;
  var buttons = compact(spinning(ctrl) || [
    button.followUp(ctrl)
  ]);
  return [
    m('div.control.icons', []),
    renderSeat(ctrl, d.player, 'bottom'),
    buttons ? m('div.control.buttons', buttons) : null,
  ];
}

function renderTablePlay(ctrl) {
  var d = ctrl.data;

  var buttons = compact();

  // debug
  // m('button', {
  //   onclick: function() { ctrl.saveBoard(); }
  // }, 'save'),


  var icons = [
    button.move(ctrl, ctrl.okeyground.canCollectOpen, 'C', 'collectOpen', ctrl.collectOpen),
    button.move(ctrl, ctrl.okeyground.canLeaveTaken, 'L', 'leaveTaken', ctrl.leaveTaken),
    button.move(ctrl, ctrl.okeyground.canOpenSeries, 'S', 'openSeries', ctrl.openSeries),
    button.move(ctrl, ctrl.okeyground.canOpenPairs, 'P', 'openPairs', ctrl.openPairs)
  ];

  return [
    (
      m('div.control.icons', icons)
    ),
    renderSeat(ctrl, d.player, 'bottom'),
    m('div.control.buttons', buttons)
  ];
}

function renderPlayer(ctrl, player) {
  return m('div', {
    class: 'player '
  },
           renderUser(ctrl, player)
   );
}

function renderSeat(ctrl, player, position = 'top') {
  var children = [renderPlayer(ctrl, player)];

  var i = position === 'bottom' ? 1:0;
  children.splice(i, 0, renderClock(ctrl, player.side, position));

  return m('div', {
    class: 'table_seat'
  }, children);
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
    m('div.table_wrap', [
      renderSeat(ctrl, d.opponentUp),
      m('div', {
        class: 'table_middle'
      }, [
        renderSeat(ctrl, d.opponentLeft),
        visualTable(ctrl),
        renderSeat(ctrl, d.opponentRight)
      ]),
      m('div.table_play',
        game.playable(ctrl.data) ? renderTablePlay(ctrl) : renderTableEnd(ctrl)
       )
    ])
  ];
};
