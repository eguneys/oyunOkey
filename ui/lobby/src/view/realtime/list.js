import m from 'mithril';
import { util } from 'okeyground';
import { tds } from '../util';

function renderHook(ctrl, hook) {
  return m('tr', {
    key: hook.id,
    title: hook.disabled ? '' : (
      (hook.action === 'join') ? 'trans(joinTheGame)':'trans(cancel)'
    ),
    'data-id': hook.id,
    class: 'hook ' + hook.action + (hook.disabled ? ' disabled': '')
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
        class: ctrl.vm.stepping ? 'stepping' : '',
        onclick: function(e) {
          var el = e.target;
          do {
            el = el.parentNode;
            if (el.nodeName === 'TR') {
              return ctrl.clickHook(el.getAttribute('data-id'));
            }
          } while (el.nodeName !== 'TABLE');
        }
      }, renderedHooks)
    ]);
  }
};
