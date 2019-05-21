import * as hookRepo from './hookRepo';
import { make as makeStores } from './store';
import * as xhr from './xhr';
import LobbySocket from './socket';

export default function LobbyController(opts, redraw) {
  this.stepHooks = [];

  this.opts = opts;
  this.data = opts.data;
  this.data.hooks = [];
  this.pools = opts.pools;
  this.redraw = redraw;

  hookRepo.initAll(this);
  this.socket = new LobbySocket(opts.socketSend, this);

  this.stores = makeStores(this.data.me ? this.data.me.username.toLowerCase() : null);

  this.tab = this.stores.tab.get();
  this.trans = opts.trans;
};
