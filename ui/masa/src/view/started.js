import m from 'mithril';
import header from './header';
import button from './button';
import { standing } from './arena';
import { myCurrentGameId } from '../masa';

module.exports = {
  main: function(ctrl) {
    var gameId = myCurrentGameId(ctrl);
    return [
      header(ctrl),
      gameId ? m('a.is.is-after.pov.button.glowed', {
        href: '/' + gameId
      }, [
        'trans You are playing!',
        m('span.text[data-icon=G]', 'trans jointhegame')
      ]) : null,
      standing(ctrl),
    ];
  }
};
