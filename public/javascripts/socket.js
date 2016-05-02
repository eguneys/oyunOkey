// ==ClosureCompiler==
// @compilation_level ADVANCED_OPTIMIZATIONS
// ==/ClosureCompiler==

oyunkeyf.StrongSocket = function(url, version, settings) {
  var now = function() {
    return new Date().getTime();
  };

  var settings = $.extend(true, {}, oyunkeyf.StrongSocket.defaults, settings);
  var url = url;
  var version = version;
  var options = settings.options;
  var ws = null;
  var pingSchedule = null;
  var connectSchedule = null;
  var ackableMessages = [];
  var lastPingTime = now();
  var currentLag = 0;
  var averageLag = 0;
  var tryOtherUrl = false;
  var autoReconnect = true;
  var nbConnects = 0;

  var connect = function() {
    destroy();
    autoReconnect = true;

    var fullUrl = "ws://" + baseUrl() + url + "?" + $.param(settings.params);
    debug("connection attempt to " + fullUrl, true);
    try {
      if (window.WebSocket) ws = new WebSocket(fullUrl);
      else throw "[okeykeyf] no websockets found on this browser!";

      ws.onerror = function(e) {
        onError(e);
      };
      ws.onclose = function(e) {
        if (autoReconnect) {
          debug('Will autoreconnect in ' + options.autoReconnectDelay);
          scheduleConnect(options.autoReconnectDelay);
        }
      };
      ws.onopen = function() {
        debug('connected to ' + fullUrl, true);
        pingNow();
      };
      ws.onmessage = function(e) {
        var m = JSON.parse(e.data);
        if (m.t === 'n') pong();

        if (m.t === 'b') {
          m.d.forEach(function(mm) {
            handle(mm);
          });
        } else handle(m);
      };
    } catch(e) {
      onError(e);
    }
    scheduleConnect(options.pingMaxLag);
  };

  var send = function(t, d, o, again) {
    var data = d || {},
        options = o || {};
    var message = JSON.stringify({
      t: t,
      d: data
    });
    debug('send ' + message);
    try {
      ws.send(message);
    } catch (e) {
      debug('send failed');
    }
  };

  var scheduleConnect = function(delay) {
    clearTimeout(pingSchedule);
    clearTimeout(connectSchedule);
    connectSchedule = setTimeout(function() {
      tryOtherUrl = true;
      connect();
    }, delay);
  };

  var schedulePing = function(delay) {
    clearTimeout(pingSchedule);
    pingSchedule = setTimeout(pingNow, delay);
  };

  var pingNow = function() {
    clearTimeout(pingSchedule);
    clearTimeout(connectSchedule);
    try {
      ws.send(pingData());
      lastPingTime = now();
    } catch (e) {
      debug(e, true);
    }
    scheduleConnect(options.pingMaxLag);
  };

  var pong = function() {
    clearTimeout(connectSchedule);
    schedulePing(options.pingDelay);
    currentLag = now() - lastPingTime;
    if (!averageLag) averageLag = currentLag;
    else averageLag = 0.2 * (currentLag - averageLag) + averageLag;
    if (options.lagTag) {
      options.lagTag.html(Math.round(averageLag));
    }
  };

  var pingData = function() {
    return JSON.stringify({
      t: 'p',
      v: version
    });
  };

  var handle = function(m) {
    if (m.v) {
      if (m.v <= version) {
        debug('already has event ' + m.v);
        return;
      }
      if (m.v > version + 1) {
        debug("event gap detected from " + version + " to " + m.v);
        return;
      }
      version = m.v;
    }

    switch (m.t || false) {
    case false:
      break;
    case 'resync':
      oyunkeyf.reload();
      break;
    default:
      if (settings.receive) settings.receive(m.t, m.d);
      var h = settings.events[m.t];
      if (h) h(m.d || null, m);
    }
  };

  var debug = function(msg, always) {
    if (always || options.debug) {
      console.debug("[" + options.name + " " + settings.params.sri + "]", msg);
    }
  };

  var destroy = function() {
    clearTimeout(pingSchedule);
    clearTimeout(connectSchedule);
    disconnect();
    ws = null;
  };

  var disconnect = function() {
    if (ws) {
      debug("Disconnect", true);
      ws.onerror = $.noop;
      ws.onclose = $.noop;
      ws.onopen = $.noop;
      ws.onmessage = $.noop;
      ws.close();
    }
  };

  var onError = function(e) {
    options.debug = true;
    debug('error: ' + JSON.stringify(e));
    tryOtherUrl = true;
    clearTimeout(pingSchedule);
  };

  var baseUrl = function() {
    var key = options.baseUrlKey;
    var urls = options.baseUrls;
    var url = oyunkeyf.storage.get(key);

    if (!url) {
      url = urls[0];
      oyunkeyf.storage.set(key, url);
    } else if (tryOtherUrl) {
      tryOtherUrl = false;
      url = urls[(urls.indexOf(url) + 1) % urls.length];
      oyunkeyf.storage.set(key, url);
    }
    return url;
  };

  connect();
  window.addEventListener('unload', destroy);

  return {
    connect: connect,
    disconnect: disconnect,
    send: send,
    destroy: destroy,
    options: options,
    pingInterval: function() {
      return options.pingDelay + averageLag;

    },
    averageLag: function() {
      return averageLag;
    }
  };
};

oyunkeyf.StrongSocket.sri = Math.random().toString(36).substring(2);
oyunkeyf.StrongSocket.defaults = {
  events: {},
  params: {
    sri: oyunkeyf.StrongSocket.sri
  },
  options: {
    debug: true,
    name: "unnamed",
    pingMaxLag: 8000, // time to wait for pong before resetting the connection
    pingDelay: 1500,
    autoReconnectDelay: 2000,
    lagTag: false,
    baseUrls: [document.domain + ':9021'].concat(
      //[9021, 9022, 9023, 9024]
      [9022].map(function(port) {
        //return 'socket.' + document.domain + ':' + port;
        return document.domain + ':' + port;
      })),
    baseUrlKey: 'surl3'
  }
};
