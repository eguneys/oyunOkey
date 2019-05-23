import { h } from 'snabbdom';
import header from './header';
import * as pagination from '../pagination';
import { controls, standing } from './arena';
import { onInsert } from './util';

export const name = 'created';

export function table(_) {
  return;
}

export function main(ctrl) {
  const pag = pagination.players(ctrl);
  return [
    header(ctrl),
    controls(ctrl, pag),
    standing(ctrl, pag, 'created'),
    ctrl.opts.$faq ? h('div', {
      hook: onInsert(el => $(el).replaceWith(ctrl.opts.$faq))
    }) : null
  ];
}
