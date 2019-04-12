var ids = {
  created: 10,
  started: 20,
  aborted: 25,
  middleEnd: 30,
  normalEnd: 40,
  variantEnd: 70
};

module.exports = {
  statusIds: ids,
  myCurrentGameId: function(ctrl) {
    var seatId = ctrl.seatId;
    if (!seatId) return null;
    var pairing = ctrl.data.pairings.filter(function(p) {
      return p.s < ids.aborted && (
        p.u.filter((id) => id.toLowerCase() === seatId.toLowerCase())[0]
      );
    })[0];
    return pairing ? pairing.id : null;
  }
};
