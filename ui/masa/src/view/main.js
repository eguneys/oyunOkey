import m from 'mithril';

module.exports = function(ctrl) {
  return [
    m('div', {
      class: util.classSet({
        'content_box masa_box masa_show': true
      })
    })
  ];
};
