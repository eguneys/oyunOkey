import m from 'mithril';

var boardContent = m('div.og-table-wrap', m('div.og-table'));

function miniBoard(game) {
  var gameSide = game.side ? '/' + game.side : '';
  return m('a', {
    key: game.id,
    href: '/' + game.id + gameSide,
    class: 'mini_board live_' + game.id + ' parse_fen is2d',
    'data-side': game.side,
    'data-fen': game.fen,
    config: function(el, isUpdate) {
      if (!isUpdate) oyunkeyf.parseFen($(el));
    }
  }, boardContent);
}

module.exports = {
  usernameOrAnon: function(ctrl, pid) {
    var data = ctrl.data;
    var p = data.players[pid];
    return p.ai ? ctrl.trans('aiBot', p.ai) : (p.name || 'Anonymous');
  },
  player: function(p, tag) {
    var ratingDiff;
    tag = tag || 'a';
    if (p.ratingDiff > 0) ratingDiff = m('span.positive[data-icon=N]', p.ratingDiff);
    else if (p.ratingDiff < 0) ratingDiff = m('span.negative[data-icon=M]', -p.ratingDiff);
    var rating = (p.rating && p.rating > 0) ? (p.rating + p.ratingDiff): null;
    var fullName = (p.name || 'Anonymous');
    var attrs = {
      class: 'ulpt user_link' + (fullName.length > 15 ? ' long' : '')
    };
    return {
      tag: tag,
      attrs: attrs,
      children: [
        fullName,
        m('span.progress', [rating, ratingDiff])
      ]
    };
  },
  miniBoard: miniBoard
};
