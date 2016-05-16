import m from 'mithril';
import { game } from 'game';

module.exports = function(ctrl, player) {
  var d = ctrl.data;

  var rating = player.rating ? player.rating : null;

  var playerOnGameIcon = m('span.status.hint--top', {
    'data-hint': ctrl.trans(player.onGame ? 'playerHasJoinedTheGame' : 'playerHasLeftTheGame')
  }, (player.onGame || !ctrl.vm.firstSeconds) ? m('span', {
    'data-icon': (player.onGame ? '3' : '0')
  }) : null);

  return player.user ? [
    m('a', {
      class: 'text ulpt user_link ' + (player.user.online ? 'online is-green' : 'offline'),
      href: '/@/' + player.user.username,
      target: game.isPlayerPlaying(d) ? '_blank' : '_self',
      'data-icon': 'r'
    }, [
      player.user.username,
      rating ? ` (${rating})` : '',
    ]),
    playerOnGameIcon
  ] : m('span.user_link', [
    player.name || 'Anonymous',
    playerOnGameIcon
  ]);
};
