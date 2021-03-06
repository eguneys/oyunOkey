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

  // index 0 not allowed piece shift hack
  result = result.replace(/^\s/, "");

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

var iBoard = 2;

export function persistentFen(fen, oldFen) {
  var oldBoard = oldFen.split('/')[iBoard];
  var board = fen.split('/')[iBoard];
  var rest = fen.substr(board.length);

  var newBoard = board;

  var diff = boardDiff(oldBoard, newBoard);

  // fen = diff + rest;

  fen = fen.replace(board, diff);

  return fen;
}

export const fenStore = {
  get: function(fen) {
    var oldBoard = oyunkeyf.storage.get(sk);

    // make a hack fen to split
    var oldFen = "//" + oldBoard + "/";

    return persistentFen(fen, oldFen);
  },
  set: function(fen) {
    //var board = fen.split('/')[iBoard];
    var board = fen;
    oyunkeyf.storage.set(sk, board);
  }
};

export function onInsert(f) {
  return {
    insert(vnode) {
      f(vnode.elm);
    }
  };
}

export function bind(eventName, f, redraw) {
  return onInsert(el => {
    el.addEventListener(eventName, e => {
      const res = f(e);
      if (redraw) redraw();
      return res;
    });
  });
}

export function spinner() {
  return h('div.spinner', {
    'aria-label': 'loading'
  }, [
    h('svg', { attrs: { viewBox: '0 0 40 40' } }, [
      h('circle', {
        attrs: { cx: 20, cy: 20, r: 18, fill: 'none' }
      })])]);
}
