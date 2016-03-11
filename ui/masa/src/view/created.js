import m from 'mithril';
import header from './header';
import button from './button';

function table(ctrl) {
  return m('div.table');
}

function seat(ctrl) {
  return m('div.seat', [
    button.joinWithdraw(ctrl)
  ]);
}

function seats(ctrl) {
  return m('div.seats_wrap',
           m('div.seats', [
             seat(ctrl),
             m('div.middle', [
               seat(ctrl),
               table(ctrl),
               seat(ctrl)
             ]),
               seat(ctrl)
           ])
          );
};

module.exports = {
  main: function(ctrl) {
    return [
      header(ctrl),
      seats(ctrl)
    ];
  }
};
