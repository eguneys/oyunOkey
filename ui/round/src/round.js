module.exports = {
  merge: function(old, cfg) {
    var data = cfg;
    return {
      data: data,
      changes: {}
    };
  }
};
