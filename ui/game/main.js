"use strict";

var _game = _interopRequireDefault(require("./game"));

var _status = _interopRequireDefault(require("./status"));

var _uci = _interopRequireDefault(require("./uci"));

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

module.exports = {
  game: _game["default"],
  status: _status["default"],
  uci: _uci["default"]
};