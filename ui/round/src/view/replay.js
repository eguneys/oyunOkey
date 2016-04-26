import m from 'mithril';

function renderMove(step, curPly) {
  return {
    tag: 'move',
    children: ['eastkas elini seri acti']
  };
}

function renderMoves(ctrl) {

  var moves = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11];

  var rows = [];
  for (var i = 0, len = moves.length; i < len; i++) rows.push({
    tag: 'turn',
    children: [{
      tag: 'index',
      children: [i + 1]
    },
    renderMove(moves[i])
    ]
  });

  return rows;
}

module.exports = function(ctrl) {
  return m('div.replay_wrap',  m('div.replay', [
    m('div.moves', {
    }, renderMoves(ctrl))
  ]));
}
