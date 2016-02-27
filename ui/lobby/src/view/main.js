import m from 'mithril';
import renderRealtime from './realtime/main';

module.exports = function(ctrl) {
  var body;

  body = renderRealtime(ctrl);

  return [
    //m('div.tabs', renderTabs(ctrl)),
    m('div.lobby_box.' + ctrl.vm.tab, body)
  ];
};
