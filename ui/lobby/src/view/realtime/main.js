import * as list from './list';

export default function(ctrl) {
  var res = ctrl.stepHooks;
  var body = list.render(ctrl, res);

  return [
    body
  ];
};
