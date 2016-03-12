import m from 'mithril';
import { util } from 'okeyground';
import { throttle } from './util';

const { partial } = util;

var xhrConfig = function(xhr) {
  xhr.setRequestHeader('X-Requested-With', 'XMLHttpRequest');
  xhr.setRequestHeader('Accept', 'application/vnd.oyunkeyf.v1+json');
};

function uncache(url) {
  return url + '?_=' + new Date().getTime();
}

function reloadPage() {
  location.reload();
}

function masaAction(action, ctrl, side) {
  var url = ['/masa', ctrl.data.id, action].join('/');
  if (side) { url += `?side=${side}`; }

  return m.request({
    method: 'POST',
    url: url,
    config: xhrConfig
  }).then(null, reloadPage);
}

function reloadMasa(ctrl) {
  return m.request({
    method: 'GET',
    url: uncache('/masa/' + ctrl.data.id),
    config: xhrConfig
  }).then(ctrl.reload, reloadPage);
}

module.exports = {
  join: throttle(1000, false, partial(masaAction, 'join')),
  withdraw: throttle(1000, false, partial(masaAction, 'withdraw')),
  reloadMasa: throttle(2000, false, reloadMasa)
};
