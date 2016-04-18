import m from 'mithril';
import header from './header';
import button from './button';
import pagination from '../pagination';
import pairings from './pairings';
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
        ctrl.trans('youArePlaying'),
        m('span.text[data-icon=G]', ctrl.trans('joinTheGame'))
      ]) : null,
      standing(ctrl, pag, 'started'),
    ];
  },
  side: function(ctrl) {
    return pairings(ctrl);
  }
};
