module.exports = function(cfg, element) {
  var pools = [{ id: "1", var: 1, lim: 1, perf: "Yüzbir" },
               { id: "3", var: 1, lim: 3, perf: "Yüzbir" },
               { id: "5", var: 1, lim: 5, perf: "Yüzbir" },
               { id: "20", var: 1, lim: 20, perf: "Yüzbir" },
               { id: "d1", var: 2, lim: 1, perf: "Düz" },
               { id: "d3", var: 2, lim: 3, perf: "Düz" },
               { id: "d5", var: 2, lim: 5, perf: "Düz" },
               { id: "d20", var: 2, lim: 20, perf: "Düz" }];
  
  var lobby;

  var nbRoundSpread = spreadNumber(
    document.querySelector('#nb_games_in_play > strong'),
    8,
    function() {
      return oyunkeyf.socket.pingInterval();
    });

  var nbUserSpread = spreadNumber(
    document.querySelector('#nb_connected_players > strong'),
    10,
    function() {
      return oyunkeyf.socket.pingInterval();
    });


  var onFirstConnect = function() {
  };
  
  oyunkeyf.socket = oyunkeyf.StrongSocket(
    '/lobby/socket/v4',
    false, {
      receive: function(t, d) {
        lobby.socketReceive(t, d);
      },
      events: {
        n: function(nbUsers, msg) {
          nbUserSpread(msg.d);
          setTimeout(function() {
            nbRoundSpread(msg.r);
          }, oyunkeyf.socket.pingInterval() / 2);
        }
      },
      options: {
        name: 'lobby',
        onFirstConnect: onFirstConnect
      }
    },
  );

  cfg.trans = oyunkeyf.trans(cfg.i18n);
  cfg.socketSend = oyunkeyf.socket.send;
  cfg.element = element;
  cfg.pools = pools;
  lobby = OyunkeyfLobby.start(cfg);

  var $startButtons = $('.lobby__start');

  var sliderRounds = [
    1, 2, 3, 5, 10, 15, 20, 25, 30
  ];

 function sliderRound(v) {
    return v < sliderRounds.length ? sliderRounds[v] : 30;
  }

  function sliderInitVal(v, f, max) {
    for (var i = 0; i < max; i++) {
      if (f(i) == v) return i;
    }
    return 0;
  }

  function prepareForm($modal) {
    var $form = $modal.find('form');
    var $modeChoicesWrap = $form.find('.mode_choice');
    var $modeChoices = $modeChoicesWrap.find('input');
    var $casual = $modeChoices.eq(0),
        $rated = $modeChoices.eq(1);
    var $variantSelect = $form.find('#sf_variant');
    var $roundsInput = $form.find('.round_choice [name=rounds]');
    var typ = $form.data('type');
    var $submits = $form.find('.color-submits__button');

    $submits.text('KUR');

    if (typ === 'hook') {

      // var ajaxSubmit = function() {
      //   $.modal.close();
      //   var call = {
      //     url: $form.attr('action').replace(/uid-placeholder/, oyunkeyf.StrongSocket.sri),
      //     data: $form.serialize(),
      //     type: 'post'
      //   };
      //   lobby.setTab('real_time');
      //   $.ajax(call);
      //   return false;
      // };
      // $submits.click(function() {
      //   return ajaxSubmit();
      // }).attr('disabled', false);
      // $form.submit(function() {
      //   return ajaxSubmit();
      // });
    } else {
      $form.one('submit', function() {
        $submits.hide().end().append(oyunkeyf.spinnerHtml);
      });
    }
    oyunkeyf.slider().done(function() {
      $roundsInput.each(function() {
        var $input = $(this),
            $value = $input.siblings('span');
        $input.after($('<div>').slider({
          value: sliderInitVal($input.val(), sliderRound, 10),
          min: 0,
          max: 10,
          range: 'min',
          step: 1,
          slide: function(event, ui) {
            var round = sliderRound(ui.value);
            $value.text(round);
            $input.attr('value', round);
          }
        }));
      });
    });
  }

  var clickEvent = 'mousedown';

  $startButtons
    .find('a:not(.disabled)')
    .on(clickEvent, function() {

      $(this).addClass('active')
        .siblings().removeClass('active');
      
      oyunkeyf.loadCssPath('lobby.setup');
      // lobby.leavePool();
      $.ajax({
        url: $(this).attr('href'),
        success: function(html) {
          prepareForm($.modal(html, 'game-setup', () => {
            $startButtons.find('.active').removeClass('active');
          }));
        },
        error: function(res) {
          if (res.status == 400)
            alert(res.responseText);
          oyunkeyf.reload();
        }
      });
      return false;
    }).on('click', function() {
      return false;
    });

  if (['#ai', '#hook'].includes(location.hash)) {
    $startButtons
      .find('.config_' + location.hash.replace('#', ''))
      .each(function() {
        $(this).attr('href', $(this).attr('href') + location.search);
      }).trigger(clickEvent);
  }

};

function spreadNumber(el, nbSteps, getDuration) {
  var previous, displayed;
  var display = function(prev, cur, it) {
    var val = oyunkeyf.numberFormat(Math.round(((prev * (nbSteps - 1 - it)) + (cur * (it + 1))) / nbSteps));
    if (val !== displayed) {
      el.textContent = val;
      displayed = val;
    }
  };
  var timeouts = [];
  return function(nb, overrideNbSteps) {
    if (!el || (!nb && nb !== 0)) return;
    if (overrideNbSteps) nbSteps = Math.abs(overrideNbSteps);
    timeouts.forEach(clearTimeout);
    timeouts = [];
    var prev = previous === 0 ? 0 : (previous || nb);
    previous = nb;
    var interv = Math.abs(getDuration() / nbSteps);
    for (var i = 0; i < nbSteps; i++)
      timeouts.push(setTimeout(display.bind(null, prev, nb, i), Math.round(i * interv)));
  };
}
