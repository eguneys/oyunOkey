import { init } from 'snabbdom';
import klass from 'snabbdom/modules/class';
import attributes from 'snabbdom/modules/attributes';

export const patch = init([klass, attributes]);

import makeCtrl from './ctrl';
import view from './view/main';
const boot = require('./boot');

export function start(opts) {
  let vnode, ctrl;

  function redraw() {
    vnode = patch(vnode, view(ctrl));
  }

  ctrl = new makeCtrl(opts, redraw);

  const blueprint = view(ctrl);
  opts.element.innerHTML = '';
  vnode = patch(opts.element, blueprint);

  return {
    socketReceive: ctrl.socket.receive,
    setTab(tab) {
      ctrl.setTab(tab);
      ctrl.redraw();
    },
    enterPool: ctrl.enterPool,
    leavePool: ctrl.leavePool,
    redraw: ctrl.redraw
  };
}

window.onload = function() {
  boot(window['oyunkeyf_lobby'], document.querySelector('.lobby__app'));
};
