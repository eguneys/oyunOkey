// ==ClosureCompiler==
// @compilation_level ADVANCED_OPTIMIZATIONS
// ==/ClosureCompiler==

var oyunkeyf = window.oyunkeyf = window.oyunkeyf || {};

// declare now, populate later in a distinct script.
var oyunkeyf_translations = oyunkeyf_translations || [];

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

oyunkeyf.widget = function(name, prototype) {
  var contructor = $[name] = function(options, element) {
    var self = this;
    self.element = $(element);
    $.data(element, name, self);
    self.options = options;
    self._create();
  };
  contructor.prototype = prototype;
  $.fn[name] = function(method) {
    var returnValue = this;
    var args = Array.prototype.slice.call(arguments, 1);
    if (typeof method === 'string') this.each(function() {
      var instance = $.data(this, name);
      if (!$.isFunction(instance[method]) || method.charAt(0) === "_")
        return $.error("no such method '" + method + "' for " + name + " widget instance");
      returnValue = instance[method].apply(instance, args);
    });
    else this.each(function() {
      if ($.data(this, name)) return $.error("widget " + name + " already bound to " + this);
      $.data(this, name, new contructor(method, this));
    });
    return returnValue;
  }
}

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

oyunkeyf.numberFormat = (function() {
  if (window.Intl && Intl.NumberFormat) {
    var formatter = new Intl.NumberFormat();
    return function(n) {
      return formatter.format(n);
    };
  }
  return function(n) {
    return n;
  };
})();
