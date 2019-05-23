import { h } from 'snabbdom';
import { dataIcon } from './util';

function clock(ctrl) {
  const d = ctrl.data;
  if (d.isFinished) return null;

  var playersToStart = 4 - d.nbPlayers;

  var children = [];
  if (playersToStart > 0) {
    children.push(
      h('div.players',
        [h('span.shy', ctrl.trans('waitingPlayers')),
         h('span.time.text', playersToStart)]));
  } else if (d.rounds && (d.nbRounds || d.nbRounds === 0)) {
    children.push(h('div.round', [d.nbRounds, '/', d.rounds]));
  } else if (d.scores) {
    children.push(h('div.round', [d.scores, ' ', ctrl.trans('points')]));
  }

  return h('div.clock', children);
}

function image(d) {
  if (d.isFinished) return null;
  return h('i.img', {
    attrs: dataIcon('g') // '|'
  });
}

function title(ctrl) {
  var d = ctrl.data;
  return h('h1', (
    d.greatPlayer ? [
      h('a', {
        attrs: {
          href: d.greatPlayer.url,
          target: '_blank'
        }
      }, d.greatPlayer.name),
      ' ',
      ctrl.trans('theTable')
    ] : d.fullName
  ));
}

export default function(ctrl) {
  return h('div.masa__main__header', [
    image(ctrl.data),
    title(ctrl),
    clock(ctrl)
  ]);
}
