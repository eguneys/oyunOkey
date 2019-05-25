import { h } from 'snabbdom';
import * as renderUser from './user';
import * as replay from './replay';
import { game, status } from 'game';
import { renderClock } from '../clock/view';
import * as button from './button';
import renderTabs from './tabs';
import renderExpiration from './expiration';
import { renderTableScores, renderTableScoreInfo } from './scores';


function compact(x) {
  if (Object.prototype.toString.call(x) === '[object Array]') {
    var elems = x.filter(function(n) {
      return n !== undefined;
    });
    return elems.length > 0 ? elems : null;
  }
  return x;
}

function isLoading(ctrl) {
  return ctrl.loading || ctrl.redirecting;
}

function loader() { return h('i.ddloader'); }

function renderTableWith(ctrl, buttons) {
  return [
    replay.render(ctrl),
    buttons.find(x => !!x) ? h('div.rcontrols', buttons) : null
  ];
}

function renderTableEnd(ctrl) {
  return renderTableWith(ctrl, [
    isLoading(ctrl) ? loader() : button.followUp(ctrl)
  ]);
}

function renderTablePlay(ctrl) {
  var d = ctrl.data;
  var trans = ctrl.trans;
  var loading = isLoading(ctrl);
  

  var buttons = loading ? [loader()] : [
    button.sortPairs(ctrl, 'N', trans('sortSeries'), ctrl.sortSeries),
    button.sortPairs(ctrl, 'K', trans('sortPairs'), ctrl.sortPairs)
  ];

  var icons = [
    button.move(ctrl, ctrl.canCollectOpen, 'C', trans('collectOpen'), ctrl.collectOpen),
    button.move(ctrl, ctrl.canLeaveTaken, 'L', trans('leaveTaken'), ctrl.leaveTaken),
    button.move(ctrl, ctrl.canOpenSeries, 'S', trans('openSeries'), ctrl.openSeries),
    button.move(ctrl, ctrl.canOpenPairs, 'P', trans('openPairs'), ctrl.openPairs)
  ];

  return [
    replay.render(ctrl),
    h('div.rcontrols', [
      h('div.ricons', icons),
      ...buttons
    ])
  ];
}

function renderPlayer(ctrl, position) {
  const player = ctrl.data[position];
  return player.ai ? h('div.user-link.online.ruser.ruser-' + player.side, [
    h('i.line'),
    h('name', renderUser.aiName(ctrl, player.ai))
  ]) : renderUser.userHtml(ctrl, player, position);
}

function renderSeat(ctrl, position, clockPosition = 'bottom') {
  const player = ctrl.data[position];
  const playerTurnPov = game.getTurnPov(ctrl.data);

  var expiration = game.playable(ctrl.data) && renderExpiration(ctrl, position);  
  var children = [renderPlayer(ctrl, position)];


  
  
  var i = clockPosition === 'bottom' ? 1:0;
  children.splice(i, 0, renderClock(ctrl, player.side, clockPosition));

  if (expiration && playerTurnPov === position) {
    children.splice(0, 0, expiration);
  }
  

  return  h('div.rseat.seat-' + position, children);
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

export function renderTable(ctrl) {
  var d = ctrl.data;

  return [
    h('div.round__app__table'),
    renderSeat(ctrl, 'opponentLeft'),
    renderSeat(ctrl, 'opponentUp'),
    renderSeat(ctrl, 'opponentRight'),
    renderSeat(ctrl, 'player'),
    ...(game.playable(ctrl.data) ? renderTablePlay(ctrl) : renderTableEnd(ctrl))
  ];
};
