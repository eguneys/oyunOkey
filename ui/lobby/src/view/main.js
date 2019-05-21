import { h } from 'snabbdom';
import renderTabs from './tabs';
import * as renderPools from './pools';
import renderRealtime from './realtime/main';

export default function(ctrl) {
  var body, data = {};

  switch (ctrl.tab) {
  case 'pools':
    body = renderPools.render(ctrl);
    data = { hook: renderPools.hooks(ctrl) };
  case 'real_time':
    body = renderRealtime(ctrl);
  }
  return h('div.lobby__app.lobby__app-' + ctrl.tab, [
    h('div.tabs-horiz', renderTabs(ctrl)),
    h('div.lobby__app__content.l' + ctrl.tab, data, body)
  ]);
};
