import m from 'mithril';
import { util }  from 'okeyground';

function tab(ctrl, key, active, content) {
  var attrs = {
    onclick: util.partial(ctrl.setTab, key)
  };

  if (key === active) attrs.class = 'active';
  return m('a', attrs, content);
}

module.exports = {
  tabs: function(ctrl) {
    var active = ctrl.vm.tab;
    return [
      tab(ctrl, 'replay_tab', active, ctrl.trans('replay')),
      tab(ctrl, 'scores_tab', active, ctrl.trans('scores')),
    ];
  },
  panel: function(ctrl, key, content) {
    var attrs = {};
    if (key === ctrl.vm.tab) attrs.class = 'active';
    return m('div.panel', attrs, content);
  }
};
