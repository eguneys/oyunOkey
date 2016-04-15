import m from 'mithril';

module.exports = function(ctrl, player) {
  var d = ctrl.data;

  var playerOnGameIcon = m('span.status.hint--top', {
    'data-hint': ctrl.trans(player.onGame ? 'playerHasJoinedTheGame' : 'playerHasLeftTheGame')
  }, (player.onGame || !ctrl.vm.firstSeconds) ? m('span', {
    'data-icon': (player.onGame ? '3' : '0')
  }) : null);

  return player.user ? [
    m('a', {
      class: 'text user_link ',
      href: '/@/ + player.user.username'
    }, [
      player.user.username
    ]),
    playerOnGameIcon
  ] : m('span.user_link', [
    player.name || 'Anonymous',
    playerOnGameIcon
  ]);
};
