window.oyunkeyf = window.oyunkeyf || {};

oyunkeyf.raf = window.requestAnimationFrame.bind(window);

oyunkeyf.storage = (function() {
  var storage = window.localStorage;
  var api = {
    get: function(k) { return storage.getItem(k); },
    set: function(k, v) { return storage.setItem(k, v); },
    remove: function(k) { return storage.removeItem(k); },
    make: function(k) {
      return {
        get: function() { return api.get(k); },
        set: function(v) { return api.get(k, v); },
        remove: function() { return api.remove(k); },
        listen: function(f) {
          window.addEventListener('storage', function(e) {
            if (e.key === k &&
                e.storageArea === storage &&
                e.newValue !== null) f(e);
          });
        }
      }; 
    }    
  };
  return api;
})();

oyunkeyf.hasToReload = false;
oyunkeyf.redirectInProgress = false;
oyunkeyf.redirect = function(obj) {
  var url;
  if (typeof obj === 'string') url = obj;
  else {
    url = obj.url;
    if (obj.cookie) {
      var domain = document.domain.replace(/^.+(\.[^\.]+\.[^\.]+)$/, '$1');
      var cookie = [
        encodeURIComponent(obj.cookie.name) + '=' + obj.cookie.value,
        '; max-age=' + obj.cookie.maxAge,
        '; path=/',
        '; domain=' + domain
      ].join('');
      document.cookie = cookie;
    }
  }
  var href = '//' + location.host + '/' + url.replace(/^\//, '');
  oyunkeyf.redirectInProgress = href;
  location.href = href;
};
oyunkeyf.reload = function() {
  if (oyunkeyf.redirectInProgress) return;
  oyunkeyf.hasToReload = true;
  if (location.hash) location.reload();
  else location.href = location.href;
};
