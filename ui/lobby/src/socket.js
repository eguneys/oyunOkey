import hookRepo from './hookRepo';

export default function LobbySocket(send, ctrl) {
  this.send = send;

  this.handlers = {
    had(hook) {
      hookRepo.add(ctrl, hook);
      if (hook.action === 'cancel') ctrl.flushHooks(true);
      ctrl.redraw();
    },
    hrm(ids) {
      ids = ids.match(/.{8}/g);
      if (ids) {
        ids.forEach((id) => {
          hookRepo.remove(ctrl, id);          
        });
      }
      ctrl.redraw();
    },
    hooks(hooks) {
      hookRepo.setAll(ctrl, hooks);
      ctrl.redraw();
    }
  };

  this.realTimeIn = () => {
    this.send('hookIn');
  };

  this.realTimeOut = () => {
    this.send('hookOut');
  };

  this.receive = (type, data) => {
    if (this.handlers[type]) {
      this.handlers[type](data);
      return true;
    }
    return false;
  };
};
