function init(hook) {
  hook.action = hook.uid === oyunkeyf.socket.settings.params.sri ? 'cancel' : 'join';
}

function initAll(ctrl) {
  ctrl.data.hooks.forEach(init);
}

module.exports = {
  init: init,
  initAll: initAll,
  add: function(ctrl, hook) {
    init(hook);
    ctrl.data.hooks.push(hook);
  },
  remove: function(ctrl, id) {
    ctrl.data.hooks = ctrl.data.hooks.filter((h) => {
      return h.id !== id;
    });
    ctrl.vm.stepHooks.forEach((h) => {
      if (h.id === id) h.disabled = true;
    });
  }
};
