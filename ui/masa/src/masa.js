export function isIn(ctrl) {
  return ctrl.data.me && ctrl.data.me.active;
}

var ids = {
  created: 10,
  started: 20,
  aborted: 25,
  middleEnd: 30,
  normalEnd: 40,
  variantEnd: 70
};

export const statusIds = ids;

function myCurrentGameId(ctrl) {
  var seatId = ctrl.seatId;
  if (!seatId) return null;
  var pairing = ctrl.data.pairings.filter(function(p) {
    return p.s < ids.aborted && (
      p.u.filter((id) => id.toLowerCase() === seatId.toLowerCase())[0]
    );
  })[0];
  return pairing ? pairing.id : null;
}

