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

oyunkeyf.spinnerHtml = '<div class="spinner"><svg viewBox="0 0 40 40"><circle cx=20 cy=20 r=18 fill="none"></circle></svg></div>';

oyunkeyf.assetUrl = function(path, opts) {
  opts = opts || {};
  var baseUrl = opts.sameDomain ? '' : document.body.getAttribute('data-asset-url');
  var version = document.body.getAttribute('data-asset-version');
  return baseUrl + '/assets' + (opts.noVersion ? '' : '/_' + version) + '/' + path;
};
oyunkeyf.loadedCss = {};
oyunkeyf.loadCss = function(url) {
  if (oyunkeyf.loadedCss[url]) return;
  oyunkeyf.loadedCss[url] = true;
  $('head').append($('<link rel="stylesheet" type="text/css" />').attr('href', oyunkeyf.assetUrl(url)));
};
oyunkeyf.loadCssPath = function(key) {
  oyunkeyf.loadCss('css/' + key + '.' + $('body').data('theme') + '.' + ($('body').data('dev') ? 'dev' : 'min') + '.css');
};

oyunkeyf.loadScript = function(url, opts) {
  return $.ajax({
    dataType: "script",
    cache: true,
    url: oyunkeyf.assetUrl(url, opts)
  });
};

oyunkeyf.slider = function() {
  return oyunkeyf.loadScript(
    'javascripts/vendor/jquery-ui.slider' + '.min.js'
  );
};


oyunkeyf.numberFormat = (function() {
  var formatter = false;
  return function(n) {
    if (formatter === false) formatter = (window.Intl && Intl.NumberFormat) ? new Intl.NumberFormat() : null;
    if (formatter === null) return n;
    return formatter.format(n);
  };
})();

$.modal = function(html, cls, onClose) {
  $.modal.close();
  if (!html.clone) html = $('<div>' + html + '</div>');
  var $wrap = $('<div id="modal-wrap">')
    .html(html.clone().removeClass('none'))
    .prepend('<span class="close" data-icon="L"></span>');
  var $overlay = $('<div id="modal-overlay">')
    .addClass(cls)
    .data('onClose', onClose)
    .html($wrap);
  $wrap.find('.close').on('click', $.modal.close);
  $overlay.on('click', function(e) {
    // disgusting hack
    // dragging slider out of a modal closes the modal
    if (!$('.ui-slider-handle.ui-state-focus').length) $.modal.close();
  });
  $wrap.on('click', function(e) {
    e.stopPropagation();
  });
  $('body').addClass('overlayed').prepend($overlay);
  return $wrap;
};
$.modal.close = function() {
  $('body').removeClass('overlayed');
  $('#modal-overlay').each(function() {
    ($(this).data('onClose') || $.noop)();
    $(this).remove();
  });
};
