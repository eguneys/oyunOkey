import xhr from './xhr';

export default function(send, ctrl) {

  const handlers = {
    reload: ctrl.askReload
  };

  return {
    send,
    receive(type, data) {
      if (handlers[type]) {
        return handlers[type](data);
      }
      return false;
    }
  };
};
