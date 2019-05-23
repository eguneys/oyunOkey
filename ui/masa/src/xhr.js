import throttle from 'common/throttle';

const headers = {
  'Accept': 'application/vnd.oyunkeyf.v1+json'
};

function onFail(_1, _2, errorMessage) {
  oyunkeyf.reload();
}

function masaAction(action) {
  return function(ctrl, side) {
    var url = ['/masa', ctrl.data.id, action].join('/');
    if (side) { url += `?side=${side}`; }

    return $.ajax({
      method: 'POST',
      url: url,
      headers
    }).fail(onFail);
  };
}

function reload(ctrl) {
  return $.ajax({
    url: '/masa/' + ctrl.data.id,
    data: {},
    headers
  }).then(data => {
    ctrl.reload(data);
    ctrl.redraw();
  }, onFail);
}

export default {
  invite: throttle(1000, masaAction('invite')),
  join: throttle(1000, masaAction('join')),
  withdraw: throttle(1000, masaAction('withdraw')),
  reloadSoon: throttle(2000, reload),
  reloadNow: reload
};
