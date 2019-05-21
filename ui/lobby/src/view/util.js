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

export function tds(bits) {
  return bits.map(function(bit) {
    return h('td', [bit]);
  });
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


export const perfIcons = {
  'Yüzbir':')',
  'Düz':'',
};
