import { h } from 'snabbdom';

var scale = 8;
var now;
var startTime;
var stopTime;

function laneGrouper(t) {
  var key;
  if (t.variant.key === 'yuzbir') {
    key = 10;
  } else if (t.variant.key === 'duzokey') {
    key = 20;
  } else {
    key = 90;
  };

  if (!t.rated) {
    key+=5;
  }

  key += 4 - t.nbPlayers;

  return key;
}

function group(arr, grouper) {
  var groups = {};
  arr.forEach(function(e) {
    var g = grouper(e);
    if (!groups[g]) groups[g] = [];
    groups[g].push(e);
  });
  return Object.keys(groups).sort().map(function(k) {
    return groups[k];
  });
}

function fitLane(lane, masa2) {
  return !lane.some(function(masa1) {
    return (Math.abs(masa1.createdAt - masa2.createdAt) < 15 * 60 * 1000);
  });
}

// split lanes that have collisions, but keeps
// groups separate by not compacting existing lanes
function splitOverlapping(lanes) {
  var ret = [];
  lanes.forEach(function(lane) {
    var newLanes = [ [] ];
    lane.forEach(function(masa) {
      var collision = true;
      for (var i = 0; i < newLanes.length; i++) {
        if (fitLane(newLanes[i], masa)) {
          newLanes[i].push(masa);
          collision = false;
          break;
        }
      }
      if (collision) newLanes.push([masa]);
    });
    ret = ret.concat(newLanes);
  });
  return ret;
}

function masaClass(masa) {
  var finished = masa.status === 30 || masa.status === 20;
  var classes = {
    'tsht-rated': masa.rated,
    'tsht-casual': !masa.rated,
    'tsht-finished': finished,
    'tsht-joinable': !finished,
  };
  return classes;
}

function renderMasa(ctrl, masa) {
  masa.duration = 20;
  var width = masa.duration * scale * 2.5;
  var left = leftPos(masa.createdAt);
  var paddingLeft = 0;

  return h('a.tsht', {
    class: masaClass(masa),
    attrs: {
      href: '/masa/' + masa.id,
      style: 'width: ' + width + 'px; ' +
        'left: ' + left + 'px; ' +
        'paddingLeft: ' + paddingLeft + 'px;'
    }
  }, [
    h('span.icon', masa.perf ? {
      attrs: {
        'data-icon': masa.perf.icon,
        title: masa.perf.name
      }
    } : {}),
    h('span.body', [
      h('span.name', masa.fullName),
      h('span.infos', [
        // m('span.rounds', displayRounds(masa.rounds)),
        h('span.text', [
          masa.variant.name + ' ',
          masa.rated ? ctrl.trans('rated') : ctrl.trans('casual')
        ]),
        masa.nbPlayers ? h('span.nb-players', {
          attrs: {
          'data-icon': 'r'
          }
        }, masa.nbPlayers) : null
      ])
    ])
  ]);
}

function renderTimeline() {
  var minutesBetween = 10;
  var time = new Date(startTime);
  time.setSeconds(0);
  time.setMinutes(Math.floor(time.getMinutes() / minutesBetween) * minutesBetween);

  var timeHeaders = [];
  var count = (stopTime - startTime) / (minutesBetween * 60 * 1000);
  for (var i = 0; i < count; i++) {
    var str = timeString(time);
    timeHeaders.push(h('div.timeheader', {
      class: {
        hour: !time.getMinutes()
      },
      attrs: {
        style: 'left: ' + leftPos(time.getTime()) + 'px'
      }
    }, str));
    time.setUTCMinutes(time.getUTCMinutes() + minutesBetween);
  }

  timeHeaders.push(h('div.timeheader.now', {
    attrs: { style: 'left: ' +  leftPos(now) + 'px' }
  }));


  return h('div.timeline',
           timeHeaders);
}

// converts Date to "%H:%M" with leading zeros
function timeString(time) {
  return ('0' + time.getHours()).slice(-2) + ':' + ('0' + time.getMinutes()).slice(-2);
}

function leftPos(time) {
  return scale * (time - startTime) / 1000 / 60;
}


export default function(ctrl) {
  now = Date.now();
  startTime = now - 3 * 60 * 60 * 1000;
  stopTime = startTime + 10 * 60 * 60 * 1000;

  const data = ctrl.data();

  var masas = data.finished.concat(data.started).concat(data.created);
  data.userMasas = masas;

  // group system masas into dedicated lanes for PerfType
  var masaLanes = splitOverlapping(
    group(data.userMasas, laneGrouper));
  return h('div.masa-chart', [
    h('div.masa-chart__inner.dragscroll.', {
      hook: {
        insert: vnode => {
          const el = vnode.elm;
          var bitLater = now + (15 * 60 * 1000);
          el.scrollLeft = leftPos(bitLater - el.clientWidth / 2 / scale * 60 * 1000);
          
        }
      }
    }, [
      renderTimeline(),
      ...masaLanes.filter(function(lane) {
        return lane.length > 0;
      }).map(function(lane) {
        return h('div.masaline',
                 lane.map(function(masa) {
                   return renderMasa(ctrl, masa);
                 }));
      })
    ])
  ]);
};
