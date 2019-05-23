import view from './view';
import { init } from 'snabbdom';
import klass from 'snabbdom/modules/class';
import attributes from 'snabbdom/modules/attributes';
import dragscroll from 'dragscroll';

const patch = init([klass, attributes]);

dragscroll;

export function app(element, env) {
  let vnode, ctrl = {
    data: () => env.data,
    trans: oyunkeyf.trans(env.i18n)
  };

  function redraw() {
    vnode = patch(vnode || element, view(ctrl));
  }

  redraw();

  setInterval(redraw, 3700);

  return {
    update: d => {
      env.data = d;
      redraw();
    }
  };
};
