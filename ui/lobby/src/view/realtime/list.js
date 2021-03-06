import { h } from 'snabbdom';
import { bind, tds, perfIcons } from '../util';
import * as hookRepo from '../../hookRepo';

function renderHook(ctrl, hook) {
  const noarg = ctrl.trans.noarg;
  return h('tr.hook.' + hook.action, {
    key: hook.id,
    attrs: {
      'data-id': hook.id
    }
  }, tds([
    hook.name,
    h('span', {
      attrs: { 'data-icon': 'r' }
    }, hook.players),
    hook.rounds,
    h('span', {
      attrs: { 'data-icon': perfIcons[hook.perf] }
    }, hook.variant + ' ' + noarg(hook.ra ? 'rated' : 'casual'))
  ]));
}

function isMine(hook) {
  return hook.action === 'cancel';
}

function isNotMine(hook) {
  return !isMine(hook);
}

export function render(ctrl, allHooks) {
  const mine = allHooks.find(isMine),
        hooks = allHooks,
        render = (hook) => renderHook(ctrl, hook),
        standards = hooks.filter(isNotMine);

  const renderedHooks = [
    ...standards.map(render)
  ];

  if (mine) renderedHooks.unshift(render(mine));

  return h('table.hooks__list', [
    h('thead',
      h('tr', [
        h('th'),
        h('th', ctrl.trans('players')),
        h('th', ctrl.trans('rounds')),
        h('th', ctrl.trans('mode'))
      ])
     ),
    h('tbody', {
      class: { stepping: ctrl.stepping },
      hook: bind('click', e => {
        let el = e.target;
        do {
          el = el.parentNode;
          if (el.nodeName === 'TR') {
            ctrl.clickHook(el.getAttribute('data-id'));
            return;
          }
        } while (el.nodeName !== 'TABLE');
      }, ctrl.redraw)
    }, renderedHooks)    
  ]);

};
