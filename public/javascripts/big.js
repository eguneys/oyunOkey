var oyunkeyf = window.oyunkeyf = window.oyunkeyf || {};

oyunkeyf.StrongSocket = function(url, version, settings) {
  var self = this;
  self.settings = $.extend(true, {}, oyunkeyf.StrongSocket.defaults, settings);
  self.url = url;
  self.version = version;
  self.options = self.settings.options;
  self.autoReconnect = true;

  self.connect();
};

oyunkeyf.StrongSocket.sri = Math.random().toString(36).substring(2);
oyunkeyf.StrongSocket.defaults = {
  params: {
    sri: oyunkeyf.StrongSocket.sri
  },
  options: {
    name: "unnamed",
    pingMaxLag: 8000, // time to wait for pong before resetting the connection
    pingDelay: 1500,
    autoReconnectDelay: 2000,
    baseUrls: [document.domain + ':' + 9000].concat(
      [9000].map(function(port) {
        return 'socket.' + document.domain + ':' + port;
      })),
    baseUrlKey: 'surl3'
  }
};
oyunkeyf.StrongSocket.prototype = {
  connect: function() {
    var self = this;
    self.destroy();
    var fullUrl = "ws://" + self.baseUrl() + self.url + "?" + $.param(self.settings.params);
    self.debug("connection attempt to " + fullUrl, true);
    try {
      if (window.WebSocket) self.ws = new WebSocket(fullUrl);
      else throw "[okeykeyf] no websockets found on this browser!";

      self.ws.onerror = function(e) {
        self.onError(e);
      };
      self.ws.onclose = function(e) {
        if (self.autoReconnect) {
          self.debug('Will autoreconnect in ' + self.options.autoReconnectDelay);
          self.scheduleConnect(self.options.autoReconnectDelay);
        }
      };
      self.ws.onopen = function() {
        self.debug('connected to ' + fullUrl, true);

        self.pingNow();
      };
      self.ws.onmessage = function(e) {
        var m = JSON.parse(e.data);
        if (m.t === 'n') self.pong();
      };
    } catch(e) {
      self.onError(e);
    }
    self.scheduleConnect(self.options.pingMaxLag);
  },
  scheduleConnect: function(delay) {
    var self = this;
    clearTimeout(self.pingSchedule);
    clearTimeout(self.connectSchedule);
    self.connectSchedule = setTimeout(function() {
      self.connect();
    }, delay);
  },
  schedulePing: function(delay) {
    var self = this;
    clearTimeout(self.pingSchedule);
    self.pingSchedule = setTimeout(function() {
      self.pingNow();
    }, delay);
  },
  pingNow: function() {
    var self = this;
    clearTimeout(self.pingSchedule);
    clearTimeout(self.connectSchedule);
    try {
      self.ws.send(self.pingData());
      self.lastPingTime = self.now();
    } catch (e) {
      self.debug(e, true);
    }
    self.scheduleConnect(self.options.pingMaxLag);
  },
  pong: function() {
    var self = this;
    clearTimeout(self.connectSchedule);
    self.schedulePing(self.options.pingDelay);
    self.currentLag = self.now() - self.lastPingTime;
  },
  pingData: function() {
    return JSON.stringify({
      t: 'p',
      v: this.version
    });
  },
  now: function() {
    return new Date().getTime();
  },
  debug: function(msg, always) {
    if (always) {
      console.debug("[" + this.options.name + " " + this.settings.params.sri + "]", msg);
    }
  },
  destroy: function() {
    clearTimeout(this.pingSchedule);
    clearTimeout(this.connectSchedule);
    this.disconnect();
    this.ws = null;
  },
  disconnect: function() {
    if (this.ws) {
      this.debug("Disconnect", true);
      this.ws.onerror = $.noop;
      this.ws.onclose = $.noop;
      this.ws.onopen = $.noop;
      this.ws.onmessage = $.noop;
      this.ws.close();
    }
  },
  onError: function(e) {
    var self = this;
    self.options.debug = true;
    self.debug('error: ' + JSON.stringify(e));
    clearTimeout(self.pingSchedule);
  },
  baseUrl: function() {
    var key = this.options.baseUrlKey;
    var urls = this.options.baseUrls;
    var url;

    if (!url) {
      url = urls[0];
    }
    return url;
  }
};

;(function() {
  $(function() {
    if (oyunkeyf.lobby) startLobby(document.getElementById('hooks_wrap'), oyunkeyf.lobby);
  });

  function startLobby(element, cfg) {
    var lobby;
    oyunkeyf.socket = new oyunkeyf.StrongSocket(
      '/lobby/socket/v1',
      1, {
        options: {
          name: 'lobby'
        }
      }
    );
  }
})();
