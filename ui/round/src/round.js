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

function lastVmPly(d) {
  var step = lastStep(d);
  return [step.ply,  step.moves.length - 1];
}

function plyCompare(ply1, ply2) {
  if (ply1[0] === ply2[0]) {
    return ply1[1] - ply2[1];
  } else {
    return ply1[0] - ply2[0];
  }
}

module.exports = {
  merge: function(old, cfg) {
    var data = cfg;
    return {
      data: data,
      changes: {}
    };
  },
  lastVmPly: lastVmPly,
  lastPly: lastPly,
  lastStep: lastStep,
  replayLength,
  plyCompare
};
