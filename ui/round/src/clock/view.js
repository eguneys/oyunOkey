import m from 'mithril';
import game from 'game';

function showBar(ctrl, time) {
  return m('div', {
    class: 'bar'
  }, m('span', {
    style: {
      width: Math.max(0, Math.min(100, (time / ctrl.data.barTime) * 100)) + '%'
    }
  }));
}

module.exports = {
  showBar: showBar
};
