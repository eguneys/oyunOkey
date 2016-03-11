import m from 'mithril';
import { util } from 'okeyground';

import created from './created';

module.exports = function(ctrl) {
  var handler;
  handler = created;

  return [
    m('div', {
      class: util.classSet({
        'content_box no_padding masa_box masa_show': true
      })
    },
      handler.main(ctrl))
  ];
};
