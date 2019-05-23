import { h } from 'snabbdom';

import * as created from './created';
import * as started from './started';
import * as finished from './finished';

import { onInsert } from './util';

module.exports = function(ctrl) {
  var handler;

  if (ctrl.data.isStarted) handler = started;
  else if (ctrl.data.isFinished) handler = finished;
  else handler = created;

  return h('main.' + ctrl.opts.classes, [
    h('aside', {
      hook: onInsert(el => {
        $(el).replaceWith(ctrl.opts.$side);
        ctrl.opts.chat && oyunkeyf.makeChat(ctrl.opts.chat);
      })
    }),
    handler.table(ctrl),
    h('div.masa__main',
      h('div.box.' + handler.name, {
        class: { 
          'masa__main-finished': ctrl.data.isFinished
        }
      }, handler.main(ctrl))
     )
  ]);
};
