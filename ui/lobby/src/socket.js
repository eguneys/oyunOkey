import m from 'mithril';
import hookRepo from './hookRepo';

module.exports = function(send, ctrl) {
  this.send = send;

  const handlers = {
    had: function(hook) {
      hookRepo.add(ctrl, hook);
      if (hook.action === 'cancel') ctrl.flushHooks(true);
      m.redraw();
    },
    hrm: function(id) {
      hookRepo.remove(ctrl, id);
      m.redraw();
    }
  };

  this.receive = (type, data) => {
    if (handlers[type]) {
      handlers[type](data);
      return true;
    }
    return false;
  };
};
