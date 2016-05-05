module.exports = {
  usernameOrAnon: function(ctrl, pid) {
    var data = ctrl.data;
    var p = data.players[pid];
    return p.ai ? ctrl.trans('aiBot', p.ai) : (p.name || 'Anonymous');
  },
  player: function(name, tag) {
    var fullName = (name || 'Anonymous');
    var attrs = {
      class: 'ulpt user_link' + (fullName.length > 15 ? ' long' : '')
    };
    return {
      tag: tag,
      attrs: attrs,
      children: [
        fullName,
      ]
    };
  }
};
