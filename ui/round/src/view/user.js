import m from 'mithril';

module.exports = function(ctrl, player) {
  var d = ctrl.data;

  return player.user ? [
    m('a', {
      class: 'text user_link ',
      href: '/@/ + player.user.username'
    }, [
      player.user.username
    ])
  ] : m('span.user_link', [
    player.name || 'Anonymous'
  ]);
};
