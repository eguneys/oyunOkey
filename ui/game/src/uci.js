const transMap = {
  dm: 'uciDrawMiddle',
  dd: 'uciDiscard',
  co: 'uciCollectOpen',
  lt: 'uciLeaveTaken',
  dl: 'uciDrawLeft',
  os: 'uciOpenSeries',
  op: 'uciOpenPairs',
  dos: 'uciDropOpenSeries',
  dop: 'uciDropOpenPairs',
  dosp: 'uciDropOpenSeriesReplace',
  dopp: 'uciDropOpenPairsReplace',
  dmp: 'uciDrawMiddlePiece'
};

const regPieceReal = /[f|r|l|b|g]\d\d?/;
const regPieceRealGlobal = /[f|r|l|b|g]\d\d?/g;

const regSingle = /^([a-z]{2})$/;
const regPiece = /^([a-z]{2})(P)(.*)$/;
const regGroup = /^([a-z]{2})(G)(.*)$/;
const regDrop = /^(dop|dos)(P)([f|r|l|b|g]\d\d?)(@)(.*)$/;

function matchSingle(uci) {
  if (uci.length === 2) {
    var [_, key] = uci.match(regSingle);
    return [transMap[key]];
  }
}

function matchPiece(uci, pt) {
  var m = uci.match(regPiece);
  if (!m) return;
  var [_, key, P, p] = m;
  if (P === 'P') {
    var suffix = (key === 'dm') ? 'p' : '';
    return [transMap[key + suffix], pt(p)];
  }
}

function matchGroup(uci) {
  var m = uci.match(regGroup);
  if (!m) return;
  var [_, key, G, g] = m;
  if (G === 'G') {
    var group = g.match(regPieceRealGlobal);

    var length = group.length / 2;
    var sum = group.map(function(x) {
      return parseInt(x.match(/\d\d?/)[0]);
    }).reduce((a, b) => (a + b));

    var arg = key === 'os'?sum:length;
    return [transMap[key], arg, g];
  }
}

function matchDrop(uci, pt) {
  var m = uci.match(regDrop);
  if (!m) return;
  var [_, key, P, p, at, x] = m;
  if (P === 'P' && at === '@') {
    var suffix = x[0] === 'p' ? 'p' : '';
    return [transMap[key + suffix], pt(p)];
  }
}

function transPiece(pieceTranslator, p) {
  const colorMap = {
    r: 'red',
    b: 'blue',
    l: 'black',
    g: 'green'
  };

  return pieceTranslator(colorMap[p[0]], p.slice(1));
}

const defaultPieceTranslator = (c, n) => c + ' ' + n;

const matchers = [matchSingle, matchPiece, matchGroup, matchDrop];
function transKey(uci, pieceTranslator = defaultPieceTranslator) {
  var pt = transPiece.bind(null, pieceTranslator);
  pt = x => x;
  var result = matchers.find(m => m(uci, pt));
  if (result) return result(uci, pt);
}

module.exports = {
  transKey: transKey
};
