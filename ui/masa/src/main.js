import { init } from 'snabbdom';
import klass from 'snabbdom/modules/class';
import attributes from 'snabbdom/modules/attributes';

const patch = init([klass, attributes]);

import makeCtrl from './ctrl';
import view from './view/main';

export function start(opts) {
  opts.classes = opts.element.getAttribute('class');
  
  let vnode, ctrl;

  function redraw() {
    vnode = patch(vnode, view(ctrl));
  }

  ctrl = new makeCtrl(opts, redraw);
  
  const blueprint = view(ctrl);
  opts.element.innerHTML = '';
  vnode = patch(opts.element, blueprint);

  return {
    socketReceive: ctrl.socket.receive
  };
}
