import { h } from 'snabbdom';

export function bind(eventName, f, redraw) {
  return {
    insert(vnode) {
      vnode.elm.addEventListener(eventName, e => {
        const res = f(e);
        if (redraw) redraw();
        return res;
      });
    }
  };
}

export function onInsert(f) {
  return {
    insert: vnode => {
      f(vnode.elm);
    }
  };
}

export function miniBoard(game) {
  var gameSide = game.side ? '/' + game.side : '';
  return h('a.mini_board.parse-fen.is2d.mini-board-' + game.id, {
    key: game.id,
    attrs: {
      href: '/' + game.id + gameSide,
      'data-side': game.side,
      'data-fen': game.fen,
    },
    hook: {
      insert(vnode) {
        oyunkeyf.parseFen($(vnode.elm));
      }
    }
  }, [
    h('div.cg-wrap')
  ]);
}

export function usernameOrAnon(ctrl, pid) {
  var data = ctrl.data;
  var p = data.players[pid];
  if (!p) return 'Anonymous';

  return  p.ai ? ctrl.trans('aiBot', p.ai) : (p.name || 'Anonymous');
}

export function dataIcon(icon) {
  return { 'data-icon': icon };
}


export function playerName(p) {
  return p.name;
}

export function player(p, tag) {
  const fullName = playerName(p);

  return h('a.ulpt.user-link' + (fullName.length > 15 ? '.long':''), {
    attrs: { href: '/@/' + p.name }
  }, [
    h('span.name', fullName),
    h('span.rating', ' ' + p.rating)
  ]);

}

export function spinner() {
  return h('div.spinner', [
    h('svg', { attrs: { viewBox: '0 0 40 40' } }, [
      h('circle', {
        attrs: { cx: 20, cy: 20, r: 18, fill: 'none' }
      })
    ])
  ]);
}
