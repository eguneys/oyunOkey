import { h } from 'snabbdom';
import * as util from '../util';

export function standard(ctrl, condition, icon, hint, socketMsg, onclick) {
  // disabled if condition callback is provided and is falsy
  var enabled = () => !condition || condition(ctrl.data);
  return h('button.fbt', {
    attrs: {
      disabled: !enabled(),
      title: ctrl.trans.noarg(hint)
    },
    hook: util.bind('click', _ => {
      if (enabled())
        onclick ? onclick() : ctrl.socket.sendLoading(socketMsg);
    })
  }, [
    h('span', util.justIcon(icon))
  ]);
}

export function sortPairs(ctrl, icon, hint, onclick) {
  return h('button.fbt', {
    hook: util.bind('click', onclick)
  }, h('span', {
    attrs: {
      'data-icon': icon
    }
  }));
}

export function move(ctrl, condition, icon, hint, onclick) {
  return h('button.fbt', {
    attrs: {
      enabled: (condition && condition())
    },
    hook: util.bind('click', onclick)
  }, h('span', {
    attrs: {
      'data-icon': icon
    }
  }));
}

export function followUp(ctrl) {
  var d = ctrl.data;

  return h('div.follow-up', [
    d.game.masaId ? h('a.text.fbt.strong.glowing', {
      attrs: {
        'data-icon': 'G',
        href: '/masa/' + d.game.masaId
      },
      hook: util.bind('click', ctrl.setRedirecting)
    }, ctrl.trans('viewMasa')) : null
  ]);
}
