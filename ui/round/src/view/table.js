import m from 'mithril';
import okeyground from 'okeyground';
import renderUser from './user';
import { game } from 'game';
import button from './button';

const { classSet, partial } = okeyground.util;

function compact(x) {
  if (Object.prototype.toString.call(x) === '[object Array]') {
    var elems = x.filter(function(n) {
      return n !== undefined;
    });
    return elems.length > 0 ? elems : null;
  }
  return x;
}

function clockShowBar(ctrl, time) {
  return m('div', {
    class: 'bar'
  }, m('span', {
    style: {
      //width: Math.max(0, Math.min(100, (time / ctrl.data.barTime) * 100)) + '%'
      width: '90%'
    }
  }));
}

function renderClock(ctrl, side, position) {
  var time = 10;
  var running = true && ctrl.data.game.player === side;

  // running = true;

  return running ? [
    m('div', {
      class: 'clock clock_' + side + ' clock_' + position
    }, [
      clockShowBar(ctrl.clock, time)
    ])
  ]: null;
}

function isSpinning(ctrl) {
  return ctrl.vm.loading || ctrl.vm.redirecting;
}

function spinning(ctrl) {
  if (isSpinning(ctrl)) return m.trust(oyunkeyf.spinnerHtml);
}

function normalizeScores(scores) {
  scores = Object.keys(scores).map(k => [scores[k], k]);
  scores.sort(([a, _], [b, __]) => { if (a === 4) return 1; else if (b === 4) return 1; return a - b;});
  return scores;
}

function playerTr(ctrl, player) {
  var isLong = player.scores.length > 5;
  var mySide = ctrl.data.player.side;

  function utilPlayer(p, tag) {
    var fullName = p.name || 'Anonymous';
    var attrs = {
      class: 'user_link'
    };
    if (p.name) attrs[tag === 'a' ? 'href' : 'data-href'] = '/@/' + p.name;
    return {
      tag: tag,
      attrs: attrs,
      children: fullName
    };
  }


  var scoreTagNames = ['penalty', 'erase', 'double', 'hand'];
  var scoreTagChild = ['+101', '-101', 'x2', `+${player.hand}`];
  function scoreTag(s, i) {
    return {
      tag: scoreTagNames[s - 1],
      children: [scoreTagChild[s - 1]]
    };
  }

  var scores = normalizeScores(player.scores).map(_ => _[0]);
  return m('tr', {
    key: player.side,
    class: classSet({
      'me': player.side === mySide,
      'long': isLong
    }),
    onclick: partial(ctrl.toggleScoresheet, player.side)
  }, [
    m('td', [
      utilPlayer(player, 'span')
    ]),
    m('td.sheet', scores.map(scoreTag)),
    m('td.total', m('strong', player.total))
  ]);
}

function renderTableScoreInfo(ctrl) {
  var d = ctrl.data;
  var side = ctrl.vm.scoresheetInfo.side;
  var player = d.game.scores[side];
  var name = player.name || 'Anonymous';

  var scores = normalizeScores(player.scores);
  var scoreTagNames = ['flag', 'penalty', 'erase', 'double', 'hand'];
  var scoreTagChild = ['-', '+101', '-101', 'x2', `+${player.hand}`];
  var flagNames = ['gameEndByHand', 'gameEndByPair', 'gameEndByDiscardOkey', 'handZero', 'handOkeyLeft', 'handNotOpened', 'handOpenedPair', 'handOpenedSome'].map(ctrl.trans);
  function scoreInfoTr(score) {
    var flag = score[1] - 1;
    var s = score[0];
    return m('tr', m('td', {
      tag: scoreTagNames[s],
      children: [scoreTagChild[s]]
    }), m('th', flagNames[flag]));
  }

  return m('div.scores_info', {
    onclick: partial(ctrl.toggleScoresheet, null)
  }, m('table',
       m('thead',
         m('tr', [
           m('td', player.total),
           m('th', name)
         ])
        ),
       m('tbody', scores.map(partial(scoreInfoTr)))));
}

function renderTableScores(ctrl) {
  var d = ctrl.data;

  var scores = Object.keys(d.game.scores).map(k => {
    var s = d.game.scores[k];
    s.side = k;
    return s;
  });
  var tableBody = scores.map(partial(playerTr, ctrl));

  return m('div.scores', [
    m('p.top text', {
    }, ctrl.trans('scores')),
    m('table.slist.standing', [
      m('thead', m('tr')),
      m('tbody', tableBody)
    ])
  ]);
}

function renderTableEnd(ctrl) {
  var d = ctrl.data;
  var buttons = compact(spinning(ctrl) || [
    button.followUp(ctrl)
  ]);
  return [
    m('div.control.icons', []),
    renderSeat(ctrl, d.player, 'bottom'),
    buttons ? m('div.control.buttons', buttons) : null,
  ];
}

function renderTablePlay(ctrl) {
  var d = ctrl.data;

  var buttons = compact();

  // debug
  // m('button', {
  //   onclick: function() { ctrl.saveBoard(); }
  // }, 'save'),


  var icons = [
    button.move(ctrl, ctrl.okeyground.canCollectOpen, 'C', 'collectOpen', ctrl.collectOpen),
    button.move(ctrl, ctrl.okeyground.canLeaveTaken, 'L', 'leaveTaken', ctrl.leaveTaken),
    button.move(ctrl, ctrl.okeyground.canOpenSeries, 'S', 'openSeries', ctrl.openSeries),
    button.move(ctrl, ctrl.okeyground.canOpenPairs, 'P', 'openPairs', ctrl.openPairs)
  ];

  return [
    (
      m('div.control.icons', icons)
    ),
    renderSeat(ctrl, d.player, 'bottom'),
    m('div.control.buttons', buttons)
  ];
}

function renderPlayer(ctrl, player) {
  return m('div', {
    class: 'player ' + player.side + (player.onGame ? ' on-game' : '')
  },
           renderUser(ctrl, player)
   );
}

function renderSeat(ctrl, player, position = 'top') {
  var children = [renderPlayer(ctrl, player)];

  var i = position === 'bottom' ? 1:0;
  children.splice(i, 0, renderClock(ctrl, player.side, position));

  return  m('div.player_wrap', children);
}

function visualTable(ctrl) {
  return m('div.oyunkeyf_table_wrap', [
    m('div', {
      class: 'oyunkeyf_table'
    }, okeyground.view(ctrl.okeyground))
  ]);
}

module.exports = function(ctrl) {
  var d = ctrl.data;
  return [
    m('div.table_wrap', [
      m('div.table_side.table_left', [
        renderSeat(ctrl, d.opponentLeft),
      ]),
      m('div.table_middle', [
        m('div.table_over',
          renderSeat(ctrl, d.opponentUp)),
        visualTable(ctrl),
        m('div.table_over',
          game.playable(ctrl.data) ? renderTablePlay(ctrl) : renderTableEnd(ctrl))
      ]),
      m('div.table_side.table_right', [
        renderSeat(ctrl, d.opponentRight),
        game.playable(ctrl.data) ? null : renderTableScores(ctrl),
        ctrl.vm.scoresheetInfo.side ? renderTableScoreInfo(ctrl) : null
      ])
    ])
  ];
};
