import m from 'mithril';

var xhrConfig = function(xhr) {
  xhr.setRequestHeader('X-Requested-With', 'XMLHttpRequest');
  xhr.setRequestHeader('Accept', 'application/vnd.oyunkeyf.v1+json');
};

function uncache(url) {
  return url + '?_=' + new Date().getTime();
}

function reload(ctrl) {
  var req = m.request({
    method: 'GET',
    url: uncache(ctrl.data.url.round),
    config: xhrConfig
  });

  req.then(function() {}, function(err) {
    oyunkeyf.reload();
  });
  return req;
}

module.exports = {
  reload: reload
};
