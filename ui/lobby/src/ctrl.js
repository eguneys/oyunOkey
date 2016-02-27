import m from 'mithril';
import { util } from 'okeyground';
import socket from './socket';

module.exports = function(env) {
  this.data = env.data;

  this.socket = new socket(env.socketSend, this);

  this.vm = {
    tab: 'realtime',
    stepHooks: this.data.hooks.slice(0)
  };

  var flushHooksTimeout;

  const doFlushHooks = () => {
    this.vm.stepHooks = this.data.hooks.slice(0);
    console.log(this.vm.stepHooks.length);
    m.redraw();
  };

  this.flushHooks = (now) => {
    clearTimeout(flushHooksTimeout);
    if (now) {
      doFlushHooks();
    } else {
      this.vm.stepping = true;
      m.redraw();
      setTimeout(() => {
        this.vm.stepping = false;
        doFlushHooks();
      }, 500);
    }
    flushHooksTimeout = flushHooksSchedule();
  };

  var flushHooksSchedule = util.partial(setTimeout, this.flushHooks, 8000);
  flushHooksSchedule();
};
