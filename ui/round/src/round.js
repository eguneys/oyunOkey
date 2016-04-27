function replayLength(d) {
  d.steps.reduce((acc, step) => {
    return acc + step.moves.length;
  }, 0);
}

function lastPly(d) {
  return d.steps[d.steps.length - 1].ply;
}

function lastStep(d) {
  return d.steps[d.steps.length - 1];
}

module.exports = {
  merge: function(old, cfg) {
    var data = cfg;
    return {
      data: data,
      changes: {}
    };
  },
  lastPly: lastPly,
  lastStep: lastStep,
  replayLength
};
