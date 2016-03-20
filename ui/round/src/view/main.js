import m from 'mithril';
import okeyground from 'okeyground';
import renderTable from './table';

module.exports = function(ctrl) {
  var d = ctrl.data;
  return [
    m('div.top', [
      m('div', {
        class: 'oyunkeyf_game'
      }, [
        m('div.oyunkeyf_ground', [
          renderTable(ctrl)
        ])
      ])
    ])
  ];
};
