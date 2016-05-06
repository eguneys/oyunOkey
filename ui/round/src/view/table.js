import m from 'mithril';
import okeyground from 'okeyground';
import renderUser from './user';
import renderReplay from './replay';
import { game, status } from 'game';
import button from './button';
import renderTabs from './tabs';
import { renderTableScores, renderTableScoreInfo } from './scores';

const { classSet, partial } = okeyground.util;

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

  // running = true;

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

  var trans = ctrl.trans;
  var icons = [
    button.move(ctrl, ctrl.okeyground.canCollectOpen, 'C', trans('collectOpen'), ctrl.collectOpen),
    button.move(ctrl, ctrl.okeyground.canLeaveTaken, 'L', trans('leaveTaken'), ctrl.leaveTaken),
    button.move(ctrl, ctrl.okeyground.canOpenSeries, 'S', trans('openSeries'), ctrl.openSeries),
    button.move(ctrl, ctrl.okeyground.canOpenPairs, 'P', trans('openPairs'), ctrl.openPairs)
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
  return player.ai ? m('div.player.on-game', [
    'Bot AI' + player.ai,
    m('span.status.hint--top', {
      'data-hint': ctrl.trans('aiReady')
    }, m('span', {
      'data-icon': '3'
    }))
  ]) : m('div', {
    class: 'player ' + player.side + (player.onGame ? ' on-game' : '')
  },
           renderUser(ctrl, player)
   );
}

function renderSeat(ctrl, player, position = 'top') {
  var children = [renderPlayer(ctrl, player)];

  var i = position === 'bottom' ? 1:0;
  children.splice(i, 0, renderClock(ctrl, player.side, position));

  return  m('div.player_wrap', children);
}

function visualTable(ctrl) {
  return m('div.oyunkeyf_table_wrap', [
    m('div', {
      class: 'oyunkeyf_table'
    }, okeyground.view(ctrl.okeyground))
  ]);
}

function renderGameStatus(ctrl) {
  return [m('div.sideboard_content',
            [m('div.sideboard_panels', [
              renderTabs.panel(ctrl, 'replay_tab',renderReplay(ctrl)),
              renderTabs.panel(ctrl, 'scores_tab',
                               (game.playable(ctrl.data)) ? null : renderTableScores(ctrl))]),
             m('div.sideboard_menu', renderTabs.tabs(ctrl))]),
          ctrl.vm.scoresheetInfo.side ? renderTableScoreInfo(ctrl) : null];
}

module.exports = function(ctrl) {
  var d = ctrl.data;
  return [
    m('div.table_wrap', [
      m('div.table_side.table_left', [
        renderSeat(ctrl, d.opponentLeft),
      ]),
      m('div.table_middle', [
        m('div.table_over',
          renderSeat(ctrl, d.opponentUp)),
        visualTable(ctrl),
        m('div.table_over',
          game.playable(ctrl.data) ? renderTablePlay(ctrl) : renderTableEnd(ctrl))
      ]),
      m('div.table_side.table_right',[
        m('div', {
          config: function(el, isUpdate) {
            if (!isUpdate) $(el).html($('.game_masa').show());
          }
        }),
        renderSeat(ctrl, d.opponentRight),
        renderGameStatus(ctrl)
      ])
    ])
  ];
};
