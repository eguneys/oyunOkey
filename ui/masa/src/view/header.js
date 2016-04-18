import m from 'mithril';

function clock(ctrl) {
  var d = ctrl.data;

  if (d.isFinished) return;

  var playersToStart = 4 - d.nbPlayers;

  var children = [];
  if (playersToStart > 0) {
    children.push(
      m('div.players',
        [m('span.shy', ctrl.trans('waitingPlayers')),
         m('span.time.text', playersToStart)]));
  }

  if (d.nbRounds || d.nbRounds === 0) {
    children.push(m('div.round', [d.nbRounds, '/', d.rounds]));
  }

  return m('div.clock', children);
}

function image(d) {
  if (d.isFinished) return;
  return m('i.img', {
    'data-icon': 'm'
  });
}

function title(ctrl) {
  var d = ctrl.data;

  return m('h1', [
    d.greatPlayer ? [
      m('a', {
        href: d.greatPlayer.url,
        target: '_blank'
      }, d.greatPlayer.name)
    ] : d.fullName
  ]);
}

module.exports = function(ctrl) {
  return [
    m('div.header', [
      image(ctrl.data),
      title(ctrl),
      clock(ctrl)
    ])
  ];
};
