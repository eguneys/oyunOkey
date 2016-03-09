import m from 'mithril';
import ctrl from './ctrl';
import view from './view/main';

module.exports = function(element, opts) {
  var controller = new ctrl(opts);

  m.module(element, {
    controller: function() {
      return controller;
    },
    view: view
  });

  return {
    socketReceive: controller.socket.receive
  };
};
