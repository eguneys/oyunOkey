import m from 'mithril';
import button from './button';

module.exports = {
  standing: function(ctrl) {
    return m('div.standing_wrap',
             m('table.slist.standing', [
               m('thead',
                 m('tr',
                   m('th.pager[colspan=3]', [
                     button.joinWithdraw(ctrl)
                   ])
                  ))
             ])
            );
  }
};
