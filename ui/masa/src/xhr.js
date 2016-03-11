import m from 'mithril';
import { throttle } from './util';

function uncache(url) {
  return url + '?_=' + new Date().getTime();
}

function reloadPage() {
  location.reload();
}

function masaAction(action, ctrl) {
  return m.request({
    method: 'POST',
    url: '/masa' + ctrl.data.id + '/' + action
  }).then(null, reloadPage);
}

function reloadMasa(ctrl) {
  return m.request({
    method: 'GET',
    url: uncache('/masa/' + ctrl.data.id),
    data: {
    }
  }).then(ctrl.reload, reloadPage);
}

module.exports = {
  reloadMasa: throttle(2000, false, reloadMasa)
};
