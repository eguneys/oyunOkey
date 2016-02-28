import ctrl from './ctrl';
import view from './view/main';
import m from 'mithril';

module.exports = function(opts) {

  var controller = new ctrl(opts);

  m.module(opts.element, {
    controller: function() {
      return controller;
    },
    view: view
  });

  return {
    socketReceive: controller.socket.receive
  };
};
