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
    baseUrls: [document.domain + ':' + 9663].concat(
      [9663].map(function(port) {
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

        if (m.t === 'b') {
          m.d.forEach(function(mm) {
            self.handle(mm);
          });
        } else self.handle(m);
      };
    } catch(e) {
      self.onError(e);
    }
    self.scheduleConnect(self.options.pingMaxLag);
  },
  send: function(t, d, o, again) {
    var self = this;
    var data = d || {},
        options = o || {};
    var message = JSON.stringify({
      t: t,
      d: data
    });
    self.debug('send ' + message);
    try {
      self.ws.send(message);
    } catch (e) {
      self.debug('send failed');
    }
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
  handle: function(m) {
    var self = this;
    if (m.v) {
      if (m.v <= self.version) {
        self.debug('already has event ' + m.v);
        return;
      }
      if (m.v > self.version + 1) {
        self.debug("event gap detected from " + self.version + " to " + m.v);
        return;
      }
      self.version = m.v;
    }

    switch (m.t || false) {
    case false:
      break;
    case 'resync':
      oyunkeyf.reload();
      break;
    default:
      if (self.settings.receive) self.settings.receive(m.t, m.d);
      var h = self.settings.events[m.t];
      if (h) h(m.d || null);
    }
  },
  now: function() {
    return new Date().getTime();
  },
  debug: function(msg, always) {
    if (always || this.options.debug) {
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

  $.redirect = function(obj) {
    var url;
    if (typeof obj == "string") url = obj;
    else {
      url = obj.url;
    }
    var href = 'http://' + location.hostname + ':' + location.port + '/' + url.replace(/^\//, '');
    $.redirect.inProgress = href;
    location.href = href;
  };

  oyunkeyf.socket = null;
  $.extend(true, oyunkeyf.StrongSocket.defaults, {
    events: {
      redirect: function(o) {
        setTimeout(function() {
          oyunkeyf.hasToReload = true;
          $.redirect(o);
        }, 300);
      }
    },
    params: {},
    options: {
      name: "site"
    }
  });

  $(function() {
    if (oyunkeyf.lobby) startLobby(document.getElementById('hooks_wrap'), oyunkeyf.lobby);
    else if (oyunkeyf.masa) startMasa(document.getElementById('masa'), oyunkeyf.masa);

    // delay so round starts first (just for perceived perf)
    setTimeout(function() {

      function setMoment() {
        $("time.moment").removeClass('moment').each(function() {
          var parsed = moment(this.getAttribute('datetime'));
          var format = this.getAttribute('data-format');
          this.textContent = format === 'calendar' ? parsed.calendar() : parsed.format(format);
        });
      }
      setMoment();

      // Zoom
      var getZoom = function() {
        return 1;
      };

      var setZoom = function(zoom) {
        var $oyunkeyfGame = $('.oyunkeyf_game, .board_and_ground');
        var px = function(i) {
          return Math.round(i) + 'px';
        };

        if ($oyunkeyfGame.length) {
          // if on a board with a game
          $('body > .content')
            .css("margin-left", `calc(50% - ${px(246.5 + 256 * zoom)})`);
        }
      };
    }, 50);
  });

  oyunkeyf.startRound = function(element, cfg) {
    var data = cfg.data;
    var round;
    oyunkeyf.socket = new oyunkeyf.StrongSocket(
      data.url.socket,
      data.player.version, {
        options: {
          name: 'round'
        },
        receive: function(t, d) {
          round.socketReceive(t, d);
        }
      });
    cfg.element = element.querySelector('.round');
    cfg.socketSend = oyunkeyf.socket.send.bind(oyunkeyf.socket);
    round = OyunkeyfRound(cfg);

    if (!data.player.spectator && data.game.status.id < 25) {
      oyunkeyf.storage.set('last-game', data.game.id);
    }
  };

  function startLobby(element, cfg) {
    var lobby;
    oyunkeyf.socket = new oyunkeyf.StrongSocket(
      '/lobby/socket/v1',
      cfg.data.version, {
        receive: function(t, d) {
          lobby.socketReceive(t, d);
        },
        events: {
          redirect: function(e) {
            $.redirect(e);
          }
        },
        options: {
          name: 'lobby'
        }
      }
    );
    cfg.socketSend = oyunkeyf.socket.send.bind(oyunkeyf.socket);
    lobby = OyunkeyfLobby(element, cfg);

    var $startButtons = $('#start_buttons');

    var sliderRounds = [1, 5, 10, 15, 20, 25, 30];

    function sliderRound(v) { return v < sliderRounds.length ? sliderRounds[v] : 30; }

    function showRound(v) {
      return v;
    }

    function sliderInitVal(v, f, max) {
      for (var i = 0; i < max; i++) {
        if (f(i) === v) return i;
      }
    }

    function prepareForm() {
      var $form = $('.oyunkeyf_overboard');
      var $formTag = $form.find('form');
      var $roundInput = $form.find('.round_choice input');
      if (false) {
        var ajaxSubmit = function() {
          $.ajax({
            url: $formTag.attr('action').replace(/uid-placeholder/, oyunkeyf.StrongSocket.sri),
            data: $formTag.serialize(),
            type: 'post'
          });
          $form.find('a.close').click();
          return false;
        };
        $formTag.find('button').click(function() {
          return ajaxSubmit();
        });
      } else {
        $form.find('form').one('submit', function() {
          $(this).find('.submits').find('button').hide().end().append(oyunkeyf.spinnerHtml);
        });
      }

      oyunkeyf.slider().done(function() {
        $roundInput.each(function() {
          var $input = $(this);
          var $value = $input.siblings('span');
          var isRoundSlider = $input.parent().hasClass('round_choice');
          $input.hide().after($('<div>').slider({
            value: sliderInitVal(parseInt($input.val()), sliderRound, 100),
            min: 0,
            max: sliderRounds.length - 1,
            range: 'min',
            step: 1,
            slide: function(event, ui) {
              var round = sliderRound(ui.value);
              $value.text(showRound(round));
              $input.attr('value', round);
            }
          }));
        });
      });

      $form.find('a.close.icon').click(function() {
        $form.remove();
        $startButtons.find('a.active').removeClass('active');
        return false;
      });
    }

    $startButtons.find('a').click(function() {
      $(this).addClass('active').siblings().removeClass('active');
      $('.oyunkeyf_overboard').remove();
      $.ajax({
        url: $(this).attr('href'),
        success: function(html) {
          $('.oyunkeyf_overboard').remove();
          //$('#hooks_wrap').prepend(html);
          $('#enterable_masas').prepend(html);
          prepareForm();
        },
        error: function() {
          location.reload();
        }
      });
      return false;
    });
  }

  function startMasa(element, cfg) {
    $('body').data('masa-id', cfg.data.id);
    var masa;
    oyunkeyf.socket = new oyunkeyf.StrongSocket(
      '/masa/' + cfg.data.id + '/socket/v1', cfg.data.socketVersion, {
        receive: function(t, d) {
          masa.socketReceive(t, d);
        },
        events: {
        },
        options: {
          name: "masa"
        }
      });
    cfg.socketSend = oyunkeyf.socket.send.bind(oyunkeyf.socket);

    masa = OyunkeyfMasa(element, cfg);
  }
})();
