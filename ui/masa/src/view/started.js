import { h } from 'snabbdom';
import header from './header';
import button from './button';
import * as pagination from '../pagination';
import pairings from './pairings';
import masaTable from './table';
import { controls, standing } from './arena';

function joinTheGame(ctrl, gameId) {
  return h('a.masa__ur-playing.button.is.is-after.glowing', {
    attrs: { href: '/' + gameId }
  }, [
    ctrl.trans('youArePlaying'), h('br'),
    ctrl.trans('joinTheGame')
  ]);
}

export function main(ctrl) {
  var gameId = ctrl.myGameId(ctrl);
  var pag = pagination.players(ctrl);

  return [
    header(ctrl),
    gameId ? joinTheGame(ctrl, gameId) : null,
    controls(ctrl, pag),
    standing(ctrl, pag, 'started'),
  ];
}

export function table(ctrl) {
  return masaTable(ctrl);
}
