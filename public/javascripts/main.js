// ==ClosureCompiler==
// @compilation_level ADVANCED_OPTIMIZATIONS
// ==/ClosureCompiler==

;(function() {

  $.userLink = function(u) {
    return $.userLinkLimit(u, false);
  };

  $.userLinkLimit = function(u, limit, klass) {
    var split = u.split(' ');
    var id = split.length == 1 ? split[0] : split[1];
    return (u || false) ? '<a class="user_link ulpt ' + (klass || '') +
      '" href="/@/' + id + '">' +
      ((limit || false) ? u.substring(0, limit) : u) + '</a>' : 'Anonymous';
  };

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

  $.fp = {};

  $.spreadNumber = function(el, nbSteps, getDuration) {
    var previous, displayed;
    var display = function(prev, cur, it) {
      var val = oyunkeyf.numberFormat(Math.round(((prev * (nbSteps - 1 - it)) + (cur * (it +  1))) / nbSteps));
      if (val !== displayed) {
        el.textContent = val;
        displayed = val;
      }
    };
    var timeouts = [];
    return function(nb) {
      if (!el || !nb) return;
      timeouts.forEach(clearTimeout);
      timeouts = [];
      var prev = previous || nb;
      previous = nb;
      var interv = getDuration() / nbSteps;
      for (var i = 0; i < nbSteps; i++) {
        timeouts.push(setTimeout(display.bind(null, prev, nb, i), Math.round(i * interv)));
      }
    };
  };

  oyunkeyf.socket = null;
  $.extend(true, oyunkeyf.StrongSocket.defaults, {
    events: {
      message: function(msg) {
        $('#chat').chat("append", msg);
      },
      mlat: function(e) {
        var $t = $('#top .server strong');
        if ($t.is(':visible')) {
          $t.text(e);
          var l = parseInt(e || 0) + parseInt(oyunkeyf.socket.options.lagTag.text()) - 100;
          var ratio = Math.max(Math.min(l / 1200, 1), 0);
          var hue = ((1 - ratio) * 120).toString(10);
          var color = ['hsl(', hue, ',100%,40%)'].join('');
          $('$top .status .led').css('background', color);
        }
      },
      redirect: function(o) {
        setTimeout(function() {
          oyunkeyf.hasToReload = true;
          $.redirect(o);
        }, 300);
      },
      masaReminder: function(data) {
        if (!$('#masa_reminder').length && !$('body').data('masa-id')) {
          $('#notifications').append(data.html).find('a.withdraw').click(function() {
            $.post($(this).attr("href"));
            $('#masa_reminder').remove();
            return false;
          });
          $('body').trigger('oyunkeyf.content_loaded');
        }
      }
    },
    params: {},
    options: {
      name: "site",
      lagTag: $('#top .ping strong')
    }
  });

  oyunkeyf.hasToReload = false;
  oyunkeyf.reload = function() {
    if ($.redirect.inProgress) return;
    oyunkeyf.hasToReload = true;
    if (window.location.hash) location.reload();
    else location.href = location.href;
  };

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
      $('body').on('oyunkeyf.content_loaded', setMoment);

      function setMomentFromNow() {
        $("time.moment-from-now").each(function() {
          this.textContent = moment(this.getAttribute('datetime')).fromNow();
        });
      }
      setMomentFromNow();
      $('body').on('oyunkeyf.content_loaded', setMomentFromNow);
      setInterval(setMomentFromNow, 2000);

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
            //.css("margin-left", `calc(50% - ${px(246.5 + 256 * zoom)})`);
            .css("margin-left", 'calc(50% - px(' + 246.5 + 256 * zoom + '))');
        }
      };
    }, 50);

    function translateTexts() {
      $('.trans_me').each(function() {
        $(this).removeClass('trans_me');
        if ($(this).val()) $(this).val($.trans($(this).val()));
        else $(this).text($.trans($(this).text()));
      });
    }
    translateTexts();
    $('body').on('oyunkeyf.content_loaded', translateTexts);

    // user profile toggle
    $('#top').on('click', 'a.toggle', function() {
      var $p = $(this).parent();
      $p.toggleClass('shown');
      $p.siblings('.shown').removeClass('shown');
      setTimeout(function() {
        var handler = function(e) {
          if ($.contains($p[0], e.target)) return;
          $p.removeClass('shown');
          $('html').off('click', handler);
        };
        $('html').on('click', handler);
      }, 10);
      // if ($p.hasClass('auth')) oyunkeyf.socket.send('moveLat', true);// ??
      return false;
    });
  });

  function urlToLink(text) {
    var exp = /\bhttp:\/\/(?:[a-z]{0, 3}\.)?(oyunkeyf\.net[-A-Z0-9+&@#\/%?=~_|!:,.;]*[-A-Z0-9+&@#\/%=~_|])/ig;
    return text.replace(exp, "<a href='http://$1'>$1</a>");
  }

  $.trans = function() {
    var str = oyunkeyf_translations[arguments[0]];
    if (!str) return arguments[0];
    Array.prototype.slice.call(arguments, 1).forEach(function(arg) {
      str = str.replace('%s', arg);
    });
    return str;
  }

  oyunkeyf.widget("chat", {
    _create: function() {
      this.options = $.extend({
        messages: [],
        initialNote: '',
        gameId: null
      }, this.options);
      var self = this;
      var $parent = self.element.parent();
      self.$msgs = self.element.find('.messages');
      self.withMsgs = !!self.$msgs.length;
      if (self.withMsgs) {
        self.$msgs.on('click', 'a', function() {
          $(this).attr('target', '_blank');
        });
        var $form = self.element.find('form');
        var $input = self.element.find('input.oyunkeyf_say')
            .focus(function() {
              document.body.classList.add('typing');
              warning();
            }).blur(function() {
              document.body.classList.remove('typing');
            });
        var warning = function() {
          // if (oyunkeyf.once('chat-nice-notice')) $input.
        };

        $form.submit(function() {
          var text = $.trim($input.val());
          if (!text) return false;
          if (text.length > 140) {
            alert('Max length: 140 chars. ' + text.length + ' chars used.');
            return false;
          }
          $input.val('');
          oyunkeyf.socket.send('talk', text);
          return false;
        });

        self.element.find('a.send').click(function() {
          $input.trigger('click');
          $form.submit();
        });

        // toggle the chat
        var $toggle = $parent.find('input.toggle_chat');
        $toggle.change(function() {
          var enabled = $toggle.is(':checked');
          self.element.toggleClass('hidden', !enabled);
          if (!enabled) oyunkeyf.storage.set('nochat', 1);
          else oyunkeyf.storage.remove('nochat');
        });
        $toggle[0].checked = oyunkeyf.storage.get('nochat') != 1;
        if (!$toggle[0].checked) {
          self.element.addClass('hidden');
        }
        if (self.options.messages.length > 0) self._appendMany(self.options.messages);
      } // end if self.msgs

      $panels = self.element.find('div.chat_panels > div');
    },
    append: function(msg) {
      this._appendHtml(this._render(msg));
    },
    _appendMany: function(objs) {
      var self = this;
      var html = "";
      $.each(objs, function () { html += self._render(this); });
      self._appendHtml(html);
    },
    _render: function(msg) {
      var user, sys = false;
      if (msg.s) {
        user = '<span class="side">[' + msg.s + ']</span>';
      } else if (msg.u === 'oyunkeyf') {
        sys = true;
        user = '<span class="system"></span>';
      } else {
        user = '<span class="user">' + $.userLinkLimit(msg.u, 14) + '</span>';
      }
      return '<li class="' + (sys ? 'system trans_me' : '') +
        (msg.r ? 'troll' : '') +
        '">' + user + urlToLink(msg.t) + '</li>';
    },
    _appendHtml: function(html) {
      if (!html) return;
      this.$msgs.each(function(i, el) {
        var autoScroll = (el.scrollTop == 0 || (el.scrollTop > (el.scrollHeight - el.clientHeight - 50)));
        $(el).append(html);
        if (autoScroll) el.scrollTop = 99999;
      });
      $('body').trigger('oyunkeyf.content_loaded');
    }
  }); // end chat widget

  oyunkeyf.startRound = function(element, cfg) {
    var data = cfg.data;
    var round;
    if (data.masa) $('body').data('masa-id', data.masa.id);
    if (!data.player.spectator && data.game.status.id < 25) {
      oyunkeyf.storage.set('last-game', data.game.id);
    }

    oyunkeyf.socket = new oyunkeyf.StrongSocket(
      data.url.socket,
      data.player.version, {
        options: {
          name: 'round'
        },
        receive: function(t, d) {
          round.socketReceive(t, d);
        },
        events: {
          end: function() {
            var url = '/' + [data.game.id, data.player.side, 'sides', data.player.spectator ? 'watcher' : 'player'].join('/');
            $.ajax({
              url: url,
              success: function(html) {
                var $html = $(html);
                $('#site_header div.side').replaceWith($html.find('>.side'));
                $('body').trigger('oyunkeyf.content_loaded');
              }
            });
          }
        }
      });

    var $chat;
    cfg.element = element.querySelector('.round');
    cfg.socketSend = oyunkeyf.socket.send.bind(oyunkeyf.socket);
    round = OyunkeyfRound(cfg);
    $chat = $('#chat').chat({
      messages: data.chat,
      initialNote: data.note,
      gameId: data.game.id
    });
  };

  function startLobby(element, cfg) {
    var lobby;
    var nbRoundSpread = $.spreadNumber(
      document.querySelector('#nb_games_in_play > strong'),
      8,
      function() {
        return oyunkeyf.socket.pingInterval();
      });
    var nbUserSpread = $.spreadNumber(
      document.querySelector('#nb_connected_players > strong'),
      10,
      function() {
        return oyunkeyf.socket.pingInterval();
      });

    oyunkeyf.socket = new oyunkeyf.StrongSocket(
      '/lobby/socket/v1',
      cfg.data.version, {
        receive: function(t, d) {
          lobby.socketReceive(t, d);
        },
        events: {
          n: function(nbUsers, msg) {
            nbUserSpread(msg.d);
            setTimeout(function() {
              nbRoundSpread(msg.r);
            }, oyunkeyf.socket.pingInterval() / 2);
          },
          redirect: function(e) {
            $.redirect(e);
          },
          masas: function(data) {
            $('#enterable_masas').html(data);
            $('body').trigger('oyunkeyf.content_loaded');
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
      var $modeChoicesWrap = $form.find('.mode_choice');
      var $modeChoices = $modeChoicesWrap.find('input');
      var $casual = $modeChoices.eq(0);
      var $rated = $modeChoices.eq(1);
      console.log('kajdf', $modeChoices);
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

      $modeChoices.add($form.find('.members_only input')).on('change', function() {
        var rated = $rated.prop('checked');
        var membersOnly = $form.find('.members_only input').prop('checked');
        //$form.find('rating_range_config'
        console.log(rated, $rated[0]);
        console.log($('.oyunkeyf_overboard').find('.mode_choice').find('input').eq(1), $rated);
        $form.find('.members_only').toggle(!rated);
      }).trigger('change');

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

    if (['#hook', '#masa'].indexOf(location.hash) !== -1) {
      $startButtons
        .find('a.config_' + location.hash.replace('#', ''))
        .each(function() {
          console.log('ineach');
          $(this).attr("href", $(this).attr("href") + location.search);
        }).click();
    }
  }

  function startMasa(element, cfg) {
    $('body').data('masa-id', cfg.data.id);
    if (typeof oyunkeyf_chat !== 'undefined') $('#chat').chat({
      messages: oyunkeyf_chat
    });
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
