import m from 'mithril';
import header from './header';
import button from './button';
import pagination from '../pagination';
import { standing } from './arena';
import vUtil from './util';
import { util } from 'okeyground';

function table(ctrl) {
  return m('div.table');
}

function seat(ctrl, side) {
  var player = ctrl.data.actives[side];
  var me = player && ctrl.playerId === player.id;

  var withInvite = (ctrl.data.createdBy === ctrl.data.playerId) |
      (ctrl.data.createdBy === ctrl.userId);

  var attrs = {
    class: util.classSet({
      'me': me,
      'empty': !player,
      'invite': withInvite
    })
  };

  return m('div.seat.in', attrs, [
    player ?
      m('span.title', vUtil.usernameOrAnon(ctrl, player.id)) :
      button.orJoinSpinner(ctrl, function() {
        return m('div.buttons', [
          withInvite ? button.seatInvite(ctrl, side) : null,
          button.seatJoin(ctrl, side)
        ]);
      })
  ]);
}

function seats(ctrl, actives) {
  return m('div.seats_wrap',
           m('div.seats', [
             seat(ctrl, 'north'),
             m('div.middle', [
               seat(ctrl, 'west'),
               table(ctrl),
               seat(ctrl, 'east'),
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
      standing(ctrl, pag, 'created'),
      seats(ctrl),
      m('div.content_box_content', {
        config: function(el, isUpdate) {
          var $masaFaq = $('#masa_faq');
          if (!isUpdate && $masaFaq[0]) $(el).html($masaFaq.show());
        }
      })
    ];
  },
  side: function(ctrl) {
    return null;
  }
};
