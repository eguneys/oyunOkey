import m from 'mithril';
import header from './header';
import button from './button';
import pagination from '../pagination';
import { standing } from './arena';
import { myCurrentGameId } from '../masa';

module.exports = {
  main: function(ctrl) {
    var gameId = myCurrentGameId(ctrl);
    var pag = pagination.players(ctrl);

    return [
      header(ctrl),
      gameId ? m('a.is.is-after.pov.button.glowed', {
        href: '/' + gameId
      }, [
        'trans You are playing!',
        m('span.text[data-icon=G]', 'trans jointhegame')
      ]) : null,
      standing(ctrl, pag, 'started'),
    ];
  }
};
