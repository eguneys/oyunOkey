import m from 'mithril';
import okeyground from 'okeyground';

function visualTable(ctrl) {
  return m('div.oyunkeyf_table_wrap', [
    m('div', {
      class: 'oyunkeyf_table'
    }, okeyground.view(ctrl.okeyground))
  ]);
}

module.exports = function(ctrl) {
  var d = ctrl.data;
  return [
    m('div.top', [
      m('div', {
        class: 'oyunkeyf_game'
      }, [
        visualTable(ctrl),
        m('div.oyunkeyf_ground', [
        ])
      ])
    ])
  ];
};
