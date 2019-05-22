import * as hookRepo from './hookRepo';
import { make as makeStores } from './store';
import * as xhr from './xhr';
import LobbySocket from './socket';

export default function LobbyController(opts, redraw) {
  this.stepHooks = [];
  
  this.opts = opts;
  this.data = opts.data;
  this.data.hooks = opts.hooks;
  this.pools = opts.pools;
  this.redraw = redraw;

  hookRepo.initAll(this);
  this.socket = new LobbySocket(opts.socketSend, this);

  this.stores = makeStores(this.data.me ? this.data.me.username.toLowerCase() : null);

  this.tab = this.stores.tab.get();
  this.trans = opts.trans;

  this.setTab = (tab) => {
    if (tab !== this.tab) {
      if (tab === 'real_time') 
        this.socket.realTimeIn();
      else if (this.tab === 'real_time') {
        this.socket.realTimeOut();
        // this.data.hooks = [];
      }
      this.tab = this.stores.tab.set(tab);
    }
  };

  this.clickPool = (id) => {
    xhr.anonPoolSeek(this.pools.find(function(p) {
      return p.id === id;
    }));
  };

  this.clickHook = (id) => {
    xhr.joinHook(id);
  };
};
