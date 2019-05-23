import { h } from 'snabbdom';

function renderDuel(d) {
  return h('a.glpt', {
  }, [
    
  ]);
}

export default function(ctrl) {
  return h('div.masa__table', [
    // h('section.masa__duels', {
    // }, [
    //   h('h2', ctrl.trans('duels'))
    // ].concat(ctrl.data.duels.map(renderDuel)))
  ]);
}
