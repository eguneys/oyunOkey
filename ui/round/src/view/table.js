import m from 'mithril';
import okeyground from 'okeyground';
import renderUser from './user';
import renderReplay from './replay';
import { game, status } from 'game';
import clockView from '../clock/view';
import button from './button';
import renderTabs from './tabs';
import renderExpiration from './expiration';
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

function renderClock(ctrl, side, position) {
  var time = ctrl.clock.data.sides[side];
  var running = true && ctrl.data.game.player === side;
  return running ? [
    m('div', {
      class: 'clock clock_' + side + ' clock_' + position + ' ' +
        classSet({
          'outoftime': !time,
          'running': running,
          'emerg': time < ctrl.clock.data.emerg
        })
    }, [
      clockView.showBar(ctrl.clock, time)
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
  var trans = ctrl.trans;

  var buttons = compact(spinning(ctrl) || [
    button.sortPairs(ctrl, 'N', trans('sortSeries'), ctrl.sortSeries),
    button.sortPairs(ctrl, 'K', trans('sortPairs'), ctrl.sortPairs)
  ]);

  // debug
  // m('button', {
  //   onclick: function() { ctrl.saveBoard(); }
  // }, 'save'),


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
    m('div.seat_wrap', [
      renderSeat(ctrl, d.player, 'player', 'bottom'),
    ]),
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

function renderSeat(ctrl, player, povString, clockPosition = 'top') {
  var expiration = game.playable(ctrl.data) && renderExpiration(ctrl, povString);
  
  var children = [renderPlayer(ctrl, player)];
  
  
  var i = clockPosition === 'bottom' ? 1:0;
  children.splice(i, 0, renderClock(ctrl, player.side, clockPosition));

  var expirationDom = expiration && expiration[1] === povString ? expiration[0] : null;

  children.splice(0, 0, expirationDom);
  

  return  m('div.player_wrap', children);
}

function visualTable(ctrl) {
  return m('div.oyunkeyf_table_wrap', [
    m('div', {
      class: 'oyunkeyf_table'
    }, okeyground.view(ctrl.okeyground))
  ]);
}

function renderGameStatusWithPanels(ctrl) {
  return [m('div.sideboard_content',
            [m('div.sideboard_panels', [
              renderTabs.panel(ctrl, 'replay_tab',renderReplay(ctrl)),
              renderTabs.panel(ctrl, 'scores_tab',
                               (game.playable(ctrl.data)) ? null : renderTableScores(ctrl))]),
             m('div.sideboard_menu', renderTabs.tabs(ctrl))]),
          ctrl.vm.scoresheetInfo.side ? renderTableScoreInfo(ctrl) : null];
}

function renderGameStatus(ctrl) {
  return m('div.replay_wrap',
           renderReplay(ctrl));
}

module.exports = function(ctrl) {
  var d = ctrl.data;

  return [
    m('div.table_wrap', [
      m('div.table_side.table_left', [
        renderSeat(ctrl, d.opponentLeft, 'opponentLeft'),
      ]),
      m('div.table_middle', [
        m('div.table_over', [
          renderSeat(ctrl, d.opponentUp, 'opponentUp')]),
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
        renderSeat(ctrl, d.opponentRight, 'opponentRight'),
        renderGameStatus(ctrl)
      ])
    ])
  ];
};
