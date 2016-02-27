import m from 'mithril';
import { util } from 'okeyground';
import { tds } from '../util';

function renderHook(ctrl, hook) {
  return m('tr', {
    'data-id': hook.id,
    class: 'hook'
  }, tds([
    m('span', {
      class: 'is'
    }),
    'Anonymous'
  ]));
}

module.exports = {
  render: function(ctrl, allHooks) {
    var render = util.partial(renderHook, ctrl);

    let renderedHooks = [
      allHooks.map(render)
    ];

    return m('table.table_wrap', [
      m('thead',
        m('tr', [
        ])
       ),
      m('tbody', {
      }, renderedHooks)
    ]);
  }
};
