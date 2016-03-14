module.exports = {
  myCurrentGameId: function(ctrl) {
    if (!ctrl.playerId) return null;
    var pairing = ctrl.data.pairings.filter(function(p) {
      return p.s === 0 && (
        p.u[0].toLowerCase() === ctrl.playerId || p.u[1].toLowerCase() === ctrl.playerId
      );
    })[0];

    return pairing ? pairing.id : null;
  }
};
