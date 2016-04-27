const sk = 'round.' + 'board';

function assertEqual(a1, a2) {
  return a1.every((k, i) => k === a2[i]);
}

const fenS = (fen) => "|" + fen + "|";
const regPiece = /[f|r|l|b|g]\d\d?/g;

function boardDiffTest() {
  function test(fen1, fen2) {
    const diff = boardDiff(fen1, fen2);
    const diffLength = diff.match(regPiece).length + (diff.match(/\s/g) || []).length;
    console.log("test \n", fenS(fen1), "\n", fenS(fen2));
    console.log(fenS(diff), diffLength);
  }
  const source = "g7b8g13r12r1r1b3b10r8r9g12b13b13g4g5r10l4g6b12g11g1g8";
  test(source, source);
  test("g7g8g13", source);
  test("               g7g8g13", source);
  test("                    g7g8g13", source);
  test("                            g7g8g13", source);
  test("                            b7 g8 g13 r1", source);
  test("               b13b13g4g5r10l4g6b12g11   b7 g8 g13 r1", source);
}

function boardDiff(oldFen, newFen) {
  if (!oldFen || !newFen || newFen === "") {
    return newFen;
  }

  var pieces = newFen.match(regPiece);
  var unusedIndexes = pieces.map((k, i) => i);

  var oldPieces = oldFen.match(/[f|r|l|g|b]\d\d?|./g);

  var piecesLength = pieces.length;

  pieces.forEach((p, i) => {
    var oldI = oldPieces.indexOf(p);
    if (oldI !== -1)
      oldPieces.splice(oldI, 1, { f: true, i: i });
  });

  oldPieces = oldPieces.map(key => {
    if (key && key.f) {
      unusedIndexes.splice(unusedIndexes.indexOf(key.i), 1);
      return pieces[key.i];
    }
    return " ";
  });

  oldPieces = oldPieces.join("");

  oldPieces = oldPieces.replace(/\s*$/, "  ");

  // var rest = unusedIndexes.map(_ => pieces[_]).join("");
  // var result = oldPieces + rest;
  // var spaces = Math.ceil(result.match(/\s/g).length / 2);
  // spaces += piecesLength;
  // while (spaces-- > 34) {
  //   //result = result.replace(/\s/, "");
  //   //result = result.replace(/\s([^\s]*)$/, "$1");
  // }

  var rest = unusedIndexes.map(_ => pieces[_]);
  var result = oldPieces + rest.slice(0).fill('  ').join("");

  rest.forEach(p => result = result.replace(/\s\s/, p));

  if (!assertEqual(result.match(regPiece).sort(), newFen.match(regPiece).sort())) {
    console.warn("board diff failed\n", fenS(result), "\n", fenS(oldFen), "\n", fenS(newFen));
    result = newFen;
  }

  return result;
}

function boardDiff2(oldFen, newFen) {

  if (!oldFen) {
    return newFen;
  }

  var pieces = newFen.match(regPiece);
  var unusedIndexes = pieces.map((k, i) => i);

  var oldFenIndexed = oldFen;

  var piecesLength = pieces.length;

  pieces.forEach((p, i) => {
    var regP = new RegExp(p);
    oldFenIndexed = oldFenIndexed.replace(regP, "${" + i + "}");
  });

  oldFenIndexed = oldFenIndexed.replace(regPiece, "");

  oldFenIndexed.match(/\${\d\d?}/g).forEach(key => {
    var i = parseInt(key.match(/\d\d?/));
    unusedIndexes.splice(unusedIndexes.indexOf(i), 1);
    oldFenIndexed = oldFenIndexed.replace(key, pieces[i]);
  });

  oldFenIndexed = oldFenIndexed.replace(/\s*$/, "  ");

  var rest = unusedIndexes.map(_ => pieces[_]).join("");

  var result = oldFenIndexed + rest;

  var spaces = result.match(/\s/g).length + piecesLength;

  while (spaces-- > 32) {
    //result = result.replace(/\s/, "");

    //result = result.replace(/\s([^\s]*)$/, "$1");
  }

  if (!assertEqual(result.match(regPiece).sort(), newFen.match(regPiece).sort())) {
    console.warn("board diff failed\n", fenS(result), "\n", fenS(oldFen), "\n", fenS(newFen));
    result = newFen;
  }

  return result;
}

function persistentFen(fen, oldFen) {
  var oldBoard = oldFen.split('/', 1)[0];
  var board = fen.split('/', 1)[0];
  var rest = fen.substr(board.length);

  var newBoard = board;

  var diff = boardDiff(oldBoard, newBoard);

  fen = diff + rest;

  return fen;
}

module.exports = {
  persistentFen: persistentFen,
  fenStore: {
    get: function(fen) {
      var oldBoard = oyunkeyf.storage.get(sk);

      var oldFen = oldBoard + "/";

      return persistentFen(fen, oldFen);
    },
    set: function(fen) {
      var board = fen.split('/', 1)[0];
      oyunkeyf.storage.set(sk, board);
    }
  },
  /**
   * https://github.com/niksy/throttle-debounce/blob/master/lib/throttle.js
   *
   * Throttle execution of a function. Especially useful for rate limiting
   * execution of handlers on events like resize and scroll.
   *
   * @param  {Number}    delay          A zero-or-greater delay in milliseconds. For event callbacks, values around 100 or 250 (or even higher) are most useful.
   * @param  {Boolean}   noTrailing     Optional, defaults to false. If noTrailing is true, callback will only execute every `delay` milliseconds while the
   *                                    throttled-function is being called. If noTrailing is false or unspecified, callback will be executed one final time
   *                                    after the last throttled-function call. (After the throttled-function has not been called for `delay` milliseconds,
   *                                    the internal counter is reset)
   * @param  {Function}  callback       A function to be executed after delay milliseconds. The `this` context and all arguments are passed through, as-is,
   *                                    to `callback` when the throttled-function is executed.
   * @param  {Boolean}   debounceMode   If `debounceMode` is true (at begin), schedule `clear` to execute after `delay` ms. If `debounceMode` is false (at end),
   *                                    schedule `callback` to execute after `delay` ms.
   *
   * @return {Function}  A new, throttled, function.
   */
  throttle: function(delay, noTrailing, callback, debounceMode) {

    // After wrapper has stopped being called, this timeout ensures that
    // `callback` is executed at the proper times in `throttle` and `end`
    // debounce modes.
    var timeoutID;

    // Keep track of the last time `callback` was executed.
    var lastExec = 0;

    // `noTrailing` defaults to falsy.
    if (typeof(noTrailing) !== 'boolean') {
      debounceMode = callback;
      callback = noTrailing;
      noTrailing = undefined;
    }

    // The `wrapper` function encapsulates all of the throttling / debouncing
    // functionality and when executed will limit the rate at which `callback`
    // is executed.
    return function() {

      var self = this;
      var elapsed = Number(new Date()) - lastExec;
      var args = arguments;

      // Execute `callback` and update the `lastExec` timestamp.
      function exec() {
        lastExec = Number(new Date());
        callback.apply(self, args);
      }

      // If `debounceMode` is true (at begin) this is used to clear the flag
      // to allow future `callback` executions.
      function clear() {
        timeoutID = undefined;
      }

      if (debounceMode && !timeoutID) {
        // Since `wrapper` is being called for the first time and
        // `debounceMode` is true (at begin), execute `callback`.
        exec();
      }

      // Clear any existing timeout.
      if (timeoutID) {
        clearTimeout(timeoutID);
      }

      if (debounceMode === undefined && elapsed > delay) {
        // In throttle mode, if `delay` time has been exceeded, execute
        // `callback`.
        exec();

      } else if (noTrailing !== true) {
        // In trailing throttle mode, since `delay` time has not been
        // exceeded, schedule `callback` to execute `delay` ms after most
        // recent execution.
        //
        // If `debounceMode` is true (at begin), schedule `clear` to execute
        // after `delay` ms.
        //
        // If `debounceMode` is false (at end), schedule `callback` to
        // execute after `delay` ms.
        timeoutID = setTimeout(debounceMode ? clear : exec, debounceMode === undefined ? delay - elapsed : delay);
      }
    };
  }
};
