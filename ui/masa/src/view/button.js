import { h } from 'snabbdom';
import { spinner, bind, dataIcon } from './util';

function orJoinSpinner(ctrl, f) {
  return ctrl.joinSpinner ? spinner() : f();
}

export function withdraw(ctrl) {
  return orJoinSpinner(ctrl, function() {
    return h('button.fbt.text', {
      attrs: dataIcon('b'),
      hook: bind('click', ctrl.withdraw, ctrl.redraw)
    }, ctrl.trans.noarg('withdraw'));
  });
}

export function join(ctrl, side) {
  return orJoinSpinner(ctrl, function() {
    return h('button.fbt.text.highlight', {
      attrs: { 'data-icon': 'G' },
      hook: bind('click', _ => {
        ctrl.join();
      }, ctrl.redraw)
    }, ctrl.trans('join'));
  });
}

export function invite(ctrl) {
  return h('button.fbt.text.invite', {
    hook: bind('click', _ => {
      ctrl.invite();
    })
  }, ctrl.trans('invite'));
}

export function joinWithdraw(ctrl) {
  return (ctrl.data.isFinished) ? null : (
    ctrl.data.me && ctrl.data.me.active ? withdraw(ctrl) : join(ctrl));
}
