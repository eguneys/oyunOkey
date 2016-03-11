import m from 'mithril';

function image(d) {
  return m('i.img', {
    'data-icon': 'm'
  });
}

function title(ctrl) {
  var d = ctrl.data;

  return m('h1', [
    d.fullName
  ]);
}

module.exports = function(ctrl) {
  return [
    m('div.header', [
      image(ctrl.data),
      title(ctrl)
    ])
  ];
};
