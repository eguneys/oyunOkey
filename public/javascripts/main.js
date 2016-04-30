// ==ClosureCompiler==
// @compilation_level ADVANCED_OPTIMIZATIONS
// ==/ClosureCompiler==

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
      $('body').on('oyunkeyf.content_loaded', setMoment);

      function setMomentFromNow() {
        $("time.moment-from-now").each(function() {
          this.textContent = moment(this.getAttribute('datetime')).fromNow();
        });
      }
      setMomentFromNow();
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

  oyunkeyf.startRound = function(element, cfg) {
    var data = cfg.data;
    var round;

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
    cfg.element = element.querySelector('.round');
    cfg.socketSend = oyunkeyf.socket.send.bind(oyunkeyf.socket);
    round = OyunkeyfRound(cfg);
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
