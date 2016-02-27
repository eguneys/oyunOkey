import m from 'mithril';
import list from './list';

module.exports = function(ctrl) {
  let res = ctrl.vm.stepHooks;
  let hooks = res;

  let body = list.render(ctrl, hooks);

  return [
    body
  ];
};
