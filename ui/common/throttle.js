"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports["default"] = throttle;

// Ensures calls to the wrapped function are spaced by the given delay.
// Any extra calls are dropped, except the last one.
function throttle(delay, callback) {
  var timer;
  var lastExec = 0;
  return function () {
    for (var _len = arguments.length, args = new Array(_len), _key = 0; _key < _len; _key++) {
      args[_key] = arguments[_key];
    }

    var self = this;
    var elapsed = Date.now() - lastExec;

    function exec() {
      timer = undefined;
      lastExec = Date.now();
      callback.apply(self, args);
    }

    if (timer) clearTimeout(timer);
    if (elapsed > delay) exec();else timer = setTimeout(exec, delay - elapsed);
  };
}