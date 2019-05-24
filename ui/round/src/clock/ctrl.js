import { updateElements } from './view';
import { game } from 'game';

function debug(data, sides) {
  var result = "clock";
  ['east', 'west', 'north', 'south'].forEach((s) => {
    result += ["\n", s, data.sides[s], sides[s]].join('|');
  });
  console.log(result);
}

const nowFun = window.performance && performance.now() > 0 ?
      performance.now.bind(performance) : Date.now;

export function ClockController(d, opts) {

  this.elements = {
    east: {},
    west: {},
    north: {},
    south: {}
  };

  this.opts = opts;

  const cdata = d.clock;

  this.timeRatioDivisor = .001 / Math.max(cdata.initial, 2);
  
  this.timeRatio = (millis) => Math.max(0, Math.min(1, millis * this.timeRatioDivisor));

  this.setClock = (d, east, west, north, south) => {
    const isClockRunning = game.playable(d) && (game.playedTurns(d) > 4);

    this.times = {
      east: east * 1000,
      west: west * 1000,
      north: north * 1000,
      south: south * 1000,
      activeSide: isClockRunning ? d.game.player : undefined,
      lastUpdate: nowFun()
    };

    if (isClockRunning) scheduleTick(this.times[d.game.player]);
  };

  const scheduleTick = (time) => {
    if (this.tickCallback !== undefined)
      clearTimeout(this.tickCallback);
    this.tickCallback = setTimeout(
      this.tick, time%100 + 1);
  };

  this.tick = () => {
    this.tickCallback = undefined;

    const side = this.times.activeSide;
    if (side === undefined) return;

    const now = nowFun();
    const millis = this.times[side] - this.elapsed(now);

    if (millis <= 0) this.opts.onFlag();
    else updateElements(this, this.elements[side], millis);

    scheduleTick(millis, 0);
  };

  this.elapsed = (now = nowFun()) => Math.max(0, now - this.times.lastUpdate);

  this.millisOf = (side) => this.times.activeSide === side ?
    Math.max(0, this.times[side] - this.elapsed()) :
    this.times[side];

  const isRunning = () => this.times.activeSide !== undefined;


  this.setClock(d, cdata.east, cdata.west, cdata.north, cdata.south);
};
