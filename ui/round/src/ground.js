import okeyground from 'okeyground';

function makeConfig(data) {
  return {

  };
}

function make(data) {
  var config = makeConfig(data);
  return new okeyground.controller(config);
}

module.exports = {
  make: make
};
