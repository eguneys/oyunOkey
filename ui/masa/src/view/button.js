import m from 'mithril';

function orJoinSpinner(ctrl, f) {
  return ctrl.vm.joinSpinner ? m.trust('oyunkeyf.spinnerHtml') : f();
}

function withdraw(ctrl) {
  return orJoinSpinner(ctrl, function() {
    return m('button.button.right.text', {
      'data-icon': 'b',
      onclick: ctrl.withdraw
    }, 'trans withdraw');
  });
}

function join(ctrl) {
  return orJoinSpinner(ctrl, function() {
    return m('button.button.right.text.glowed', {
      'data-icon': 'G',
      onclick: ctrl.join
    }, 'trans join');
  });
}

module.exports = {
  withdraw: withdraw,
  join: join,
  joinWithdraw: function(ctrl) {
    return join(ctrl);
    return (!ctrl.userId) ? null : (
      ctrl.data.me && !ctrl.data.me.withdraw ? withdraw(ctrl) : join(ctrl));
  }
};
