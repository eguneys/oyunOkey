"use strict";

function _slicedToArray(arr, i) { return _arrayWithHoles(arr) || _iterableToArrayLimit(arr, i) || _nonIterableRest(); }

function _nonIterableRest() { throw new TypeError("Invalid attempt to destructure non-iterable instance"); }

function _iterableToArrayLimit(arr, i) { var _arr = []; var _n = true; var _d = false; var _e = undefined; try { for (var _i = arr[Symbol.iterator](), _s; !(_n = (_s = _i.next()).done); _n = true) { _arr.push(_s.value); if (i && _arr.length === i) break; } } catch (err) { _d = true; _e = err; } finally { try { if (!_n && _i["return"] != null) _i["return"](); } finally { if (_d) throw _e; } } return _arr; }

function _arrayWithHoles(arr) { if (Array.isArray(arr)) return arr; }

var transMap = {
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
var regPieceReal = /[f|r|l|b|g]\d\d?/;
var regPieceRealGlobal = /[f|r|l|b|g]\d\d?/g;
var regSingle = /^([a-z]{2})$/;
var regPiece = /^([a-z]{2})(P)(.*)$/;
var regGroup = /^([a-z]{2})(G)(.*)$/;
var regDrop = /^(dop|dos)(P)([f|r|l|b|g]\d\d?)(@)(.*)$/;

function matchSingle(uci) {
  if (uci.length === 2) {
    var _uci$match = uci.match(regSingle),
        _uci$match2 = _slicedToArray(_uci$match, 2),
        _ = _uci$match2[0],
        key = _uci$match2[1];

    return [transMap[key]];
  }
}

function matchPiece(uci, pt) {
  var m = uci.match(regPiece);
  if (!m) return;

  var _m = _slicedToArray(m, 4),
      _ = _m[0],
      key = _m[1],
      P = _m[2],
      p = _m[3];

  if (P === 'P') {
    var suffix = key === 'dm' ? 'p' : '';
    return [transMap[key + suffix], pt(p)];
  }
}

function matchGroup(uci) {
  var m = uci.match(regGroup);
  if (!m) return;

  var _m2 = _slicedToArray(m, 4),
      _ = _m2[0],
      key = _m2[1],
      G = _m2[2],
      g = _m2[3];

  if (G === 'G') {
    var group = g.match(regPieceRealGlobal);
    var length = group.length / 2;
    var sum = group.map(function (x) {
      return parseInt(x.match(/\d\d?/)[0]);
    }).reduce(function (a, b) {
      return a + b;
    });
    var arg = key === 'os' ? sum : length;
    return [transMap[key], arg, g];
  }
}

function matchDrop(uci, pt) {
  var m = uci.match(regDrop);
  if (!m) return;

  var _m3 = _slicedToArray(m, 6),
      _ = _m3[0],
      key = _m3[1],
      P = _m3[2],
      p = _m3[3],
      at = _m3[4],
      x = _m3[5];

  if (P === 'P' && at === '@') {
    var suffix = x[0] === 'p' ? 'p' : '';
    return [transMap[key + suffix], pt(p)];
  }
}

function transPiece(pieceTranslator, p) {
  var colorMap = {
    r: 'red',
    b: 'blue',
    l: 'black',
    g: 'green'
  };
  return pieceTranslator(colorMap[p[0]], p.slice(1));
}

var defaultPieceTranslator = function defaultPieceTranslator(c, n) {
  return c + ' ' + n;
};

var matchers = [matchSingle, matchPiece, matchGroup, matchDrop];

function transKey(uci) {
  var pieceTranslator = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : defaultPieceTranslator;
  var pt = transPiece.bind(null, pieceTranslator);

  pt = function pt(x) {
    return x;
  };

  var result = matchers.find(function (m) {
    return m(uci, pt);
  });
  if (result) return result(uci, pt);
}

module.exports = {
  transKey: transKey
};