import m from 'mithril';

function debug(data, sides) {
  var result = "clock";
  ['east', 'west', 'north', 'south'].forEach((s) => {
    result += ["\n", s, data.sides[s], sides[s]].join('|');
  });
  console.log(result);
}

module.exports = function(data, onFlag, soundSide) {

  var lastUpdate;

  this.data = data;
  this.data.barTime = Math.max(this.data.initial, 2);

  function setLastUpdate() {
    lastUpdate = {
      east: data.sides['east'],
      west: data.sides['west'],
      north: data.sides['north'],
      south: data.sides['south'],
      at: new Date()
    };
  }
  setLastUpdate();

  this.update = (sides) => {
    m.startComputation();
    this.data.sides = sides;
    setLastUpdate();
    m.endComputation();
  };

  this.tick = (side) => {
    this.data.sides[side] =
      Math.max(0, lastUpdate[side] - (new Date() - lastUpdate.at) / 1000);
    if (this.data.sides[side] === 0) onFlag();
    m.redraw();
  };

};
