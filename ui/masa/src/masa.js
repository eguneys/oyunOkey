module.exports = {
  myCurrentGameId: function(ctrl) {
    var playerId = ctrl.playerId;
    if (!playerId) return null;
    var pairing = ctrl.data.pairings.filter(function(p) {
      return p.s === 0 && (
        p.u.filter((id) => id.toLowerCase() === playerId.toLowerCase())[0]
      );
    })[0];
    return pairing ? pairing.id : null;
  }
};
