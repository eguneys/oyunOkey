import m from 'mithril';
import okeyground from 'okeyground';

const { util, util: { partial, classSet } } = okeyground;

module.exports = {
  standard: function(ctrl, condition, icon, hint, socketMsg, onclick) {
    // disabled if condition callback is provided and is falsy
    var enabled = !condition || condition(ctrl.data);
    return m('button', {
      class: 'button hint--bottom ' + socketMsg + classSet({
        ' disabled': !enabled
      }),
      'data-hint': hint,
      onclick: enabled ? onclick || partial(ctrl.socket.sendLoading, socketMsg, null) : null
    }, m('span', {
      'data-icon': icon
    }));
  },
  move: function(ctrl, condition, icon, hint, onclick) {
    return m('button', {
      class: 'button hint--bottom move ' + util.classSet({
        enabled: (condition && condition())
      }),
      'data-hint': hint,
      onclick: onclick
    }, m('span', {
      'data-icon': icon
    }));
  }
};
