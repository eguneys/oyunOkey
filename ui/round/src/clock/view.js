import { h } from 'snabbdom';

function showBar(ctrl, els, millis) {
  const update = (el) => {
    els.bar = el;
    el.style.transform = "scale(" + ctrl.timeRatio(millis) + ",1)";
  };


  return h('div.bar', {
    hook: {
      insert: vnode => update(vnode.elm),
      postpatch: (_, vnode) => update(vnode.elm)
    }
  });
}

export function renderClock(ctrl, side, position) {
  const clock = ctrl.clock,
        millis = clock.millisOf(side),
        isPlayer = ctrl.data.player.side === side,
        isRunning = side === clock.times.activeSide;


  const update = (el) => {
    
  };

  return isRunning ?
    h('div.rclock.rclock-' + position, {
      class: {
        outoftime: millis <= 0,
        running: isRunning,
        emerg: millis < clock.emergMs
      } 
    }, [
      showBar(clock, clock.elements[side], millis)
    ]) : null;
}

export function updateElements(clock, els, millis) {
  if (els.bar) els.bar.style.transform = "scale(" + clock.timeRatio(millis) + ",1)";
}
