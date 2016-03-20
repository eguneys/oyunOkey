const sk = 'round.' + 'board';

function assertEqual(a1, a2) {
  return a1.every((k, i) => k === a2[i]);
}

function boardDiff(oldFen, newFen) {

  if (!oldFen) {
    return newFen;
  }

  var regPiece = /[r|l|b|g]\d\d?/g;

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

  while (spaces-- > 34) {
    //result = result.replace(/\s/, "");
    result = result.replace(/\s([^\s]*)$/, "$1");
  }

  if (!assertEqual(result.match(regPiece).sort(), newFen.match(regPiece).sort())) {
    console.warn("board diff failed", oldFen, newFen);
    result = newFen;
  }

  return result;
}

module.exports = {
  fenStore: {
    get: function(fen) {
      var board = fen.split('/', 1)[0];
      var rest = fen.substr(board.length);

      var oldBoard = oyunkeyf.storage.get(sk, board);

      var newBoard = board;

      var diff = boardDiff(oldBoard, newBoard);

      fen = diff + rest;

      return fen;
    },
    set: function(fen) {
      var board = fen.split('/', 1)[0];
      oyunkeyf.storage.set(sk, board);
    }
  }
};
