var oyunkeyf = window.oyunkeyf = window.oyunkeyf || {};

function withStorage(f) {
  try {
    return !!window.localStorage ? f(window.localStorage) : null;
  } catch(e) {}
};

oyunkeyf.storage = {
  get: function(k) {
    return withStorage(function(s) {
      return s.getItem(k);
    });
  },
  remove: function(k) {
    withStorage(function(s) {
      s.removeItem(k);
    });
  },
  set: function(k, v) {
    withStorage(function(s) {
      s.removeItem(k);
      s.setItem(k, v);
    });
  }
};

oyunkeyf.trans = function(i18n) {
  return function(key) {
    var str = i18n[key] || key;
    Array.prototype.slice.call(arguments, 1).forEach(function(arg) {
      str = str.replace('%s', arg);
    });
    return str;
  };
};

oyunkeyf.spinnerHtml = '<div class="spinner"><svg viewBox="0 0 40 40"><circle cx=20 cy=20 r=18 fill="none"></circle></svg></div>';

oyunkeyf.assetUrl = function(url, noVersion) {
  return $('body').data('asset-url') + url + (noVersion ? '' : '?=' + $('body').data('asset-version'));
};

oyunkeyf.loadScript = function(url, noVersion) {
  return $.ajax({
    dataType: "script",
    cache: true,
    url: oyunkeyf.assetUrl(url, noVersion)
  });
};

oyunkeyf.slider = function() {
  return oyunkeyf.loadScript('/assets/javascripts/vendor/jquery-ui.slider.min.js', true);
};
