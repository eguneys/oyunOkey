import { init } from 'snabbdom';
import klass from 'snabbdom/modules/class';
import attributes from 'snabbdom/modules/attributes';

import RoundController from './ctrl';
import { main as view } from './view/main';
import * as chat from 'chat';
import boot from './boot';

export function app(opts) {
  const patch = init([klass, attributes]);

  let vnode, ctrl;

  function redraw() {
    vnode = patch(vnode, view(ctrl));
  }

  ctrl = new RoundController(opts, redraw);

  const blueprint = view(ctrl);
  opts.element.innerHTML = '';
  vnode = patch(opts.element, blueprint);

  window.addEventListener('resize', redraw);
  
  return {
    socketReceive: ctrl.socket.receive
  };
}

export { boot };

window.OyunkeyfChat = chat;
