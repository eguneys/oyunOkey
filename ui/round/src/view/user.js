import { h } from 'snabbdom';

export function aiName(ctrl, level) {
  return ctrl.trans('aiName', level);
}

export function userHtml(ctrl, player, position) {
  const d = ctrl.data,
        user = player.user,
        rating = player.rating ? player.rating : null,
        rd = player.ratingDiff,
        ratingDiff = rd === 0 ? h('span', '±0') : (
          rd && rd > 0 ? h('good', '+' + rd) : (
            rd && rd < 0 ? h('bad', '-' + -rd) : undefined
          )
        );

  if (user) {
    return h(`div.ruser-${position}.ruser.user-link`, {
      class: {
        online: player.onGame,
        offline: !player.onGame
      }
    }, [
      h('i.line', {
        title: ctrl.trans(player.onGame ? 'playerHasJoinedTheGame' : 'playerHasLeftTheGame')
      }),
      h('a.text.ulpt', {
        attrs: {
          href: '/@/' + user.username,
          target: ctrl.isPlaying() ? '_blank' : '_self',
          'data-icon': 'r'
        }
      }, [user.username]),
      rating ? h('rating', rating) : null,
      ratingDiff
    ]);
  }

  return h('div.ruser-${position}.ruser.user-link', {
    class: {
      online: player.onGame,
      offline: !player.onGame
    }
  }, [
    h('i.line', {
      attrs: {
        title: ctrl.trans(player.onGame ? 'playerHasJoinedTheGame' : 'playerHasLeftTheGame')
      }
    }),
    h('name', player.name || 'Misafir')
  ]);
};
