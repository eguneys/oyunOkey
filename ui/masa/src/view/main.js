import m from 'mithril';
import { util } from 'okeyground';

import created from './created';
import started from './started';
import finished from './finished';

module.exports = function(ctrl) {
  var handler;
  if (ctrl.data.isStarted) handler = started;
  else if (ctrl.data.isFinished) handler = finished;
  else handler = created;

  var side = handler.side(ctrl);

  return [
    side ? m('div#masa_side', side) : null,
    m('div', {
      class: util.classSet({
        'content_box no_padding masa_box masa_show': true,
        'finished': ctrl.data.isFinished
      })
    },
      handler.main(ctrl))
  ];
};
