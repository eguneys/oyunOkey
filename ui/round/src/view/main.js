import { h } from 'snabbdom';
import okeyground from 'okeyground';
import { renderTable } from './table';
import renderCrosstable from './crosstable';
import { render as renderGround } from '../ground';

export function main(ctrl) {
  var d = ctrl.data;
  return h('div.round__app.variant-' + d.game.variant.key, { }, [
    h('div.round__app__board.main-board', {},
      [
        renderGround(ctrl)
      ]),
    ...renderTable(ctrl)
  ]);
};
