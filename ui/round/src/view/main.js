import m from 'mithril';
import okeyground from 'okeyground';
import renderTable from './table';
import renderCrosstable from './crosstable';

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
      ]),
      m('div.underboard', [
        m('div.center', renderCrosstable(ctrl))
      ])
    ])
  ];
};
