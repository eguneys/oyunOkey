import ctrl from './ctrl';
import view from './view';
import m from 'mithril';

module.exports = function(element, opts) {

  var controller = new ctrl(opts);

  m.module(element, {
    controller: function() {
      return controller;
    },
    view: view
  });

  return {
    update: controller.update
  };
};
