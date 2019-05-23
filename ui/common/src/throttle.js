// Ensures calls to the wrapped function are spaced by the given delay.
// Any extra calls are dropped, except the last one.
export default function throttle(delay, callback) {
  let timer;
  let lastExec = 0;

  return function(...args) {
    var self = this;
    const elapsed = Date.now() - lastExec;

    function exec() {
      timer = undefined;
      lastExec = Date.now();
      callback.apply(self, args);
    }

    if (timer) clearTimeout(timer);

    if (elapsed > delay) exec();
    else timer = setTimeout(exec, delay - elapsed);
  };
}
