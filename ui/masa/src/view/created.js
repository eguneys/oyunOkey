import m from 'mithril';
import header from './header';
import button from './button';
import pagination from '../pagination';
import { standing } from './arena';

function table(ctrl) {
  return m('div.table');
}

function seat(ctrl, side) {
  var player = ctrl.data.actives[side];
  return m('div.seat', [
    player ? player.id : button.join(ctrl, side)
  ]);
}

function seats(ctrl, actives) {
  return m('div.seats_wrap',
           m('div.seats', [
             seat(ctrl, 'north'),
             m('div.middle', [
               seat(ctrl, 'east'),
               table(ctrl),
               seat(ctrl, 'west'),
             ]),
             seat(ctrl, 'south')
           ])
          );
};

module.exports = {
  main: function(ctrl) {
    var pag = pagination.players(ctrl);
    return [
      header(ctrl),
      seats(ctrl),
      standing(ctrl, pag, 'created')
    ];
  }
};
