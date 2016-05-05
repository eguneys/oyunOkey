import m from 'mithril';
import { util } from 'okeyground';

function orJoinSpinner(ctrl, f) {
  return ctrl.vm.joinSpinner ? m.trust(oyunkeyf.spinnerHtml) : f();
}

function withdraw(ctrl) {
  return orJoinSpinner(ctrl, function() {
    return m('button.button.right.text', {
      'data-icon': 'b',
      onclick: ctrl.withdraw
    }, ctrl.trans('withdraw'));
  });
}

function join(ctrl, side) {
  return orJoinSpinner(ctrl, function() {
    return m('button.button.right.text.glowed', {
      'data-icon': 'G',
      onclick: util.partial(ctrl.join, side)
    }, ctrl.trans('join'));
  });
}

function seatInvite(ctrl, side) {
  return m('button.button.right.text.invite', {
    // 'data-icon': 'G',
    onclick: util.partial(ctrl.invite, side)
  }, ctrl.trans('invite'));
}

function seatJoin(ctrl, side) {
  return m('button.button.right.text', {
//    'data-icon': 'G',
    onclick: util.partial(ctrl.join, side)
  }, ctrl.trans('join'));
}

module.exports = {
  withdraw: withdraw,
  join: join,
  seatJoin: seatJoin,
  seatInvite: seatInvite,
  orJoinSpinner: orJoinSpinner,
  joinWithdraw: function(ctrl) {
    return (ctrl.data.isFinished) ? null : (
      ctrl.data.me && ctrl.data.me.active ? withdraw(ctrl) : join(ctrl));
  }
};
