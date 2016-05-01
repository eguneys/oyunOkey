module.exports = {
  usernameOrAnon: function(data, pid) {
    var p = data.users[pid];
    return p || 'Anonymous';
  },
  player: function(p, tag) {
    var fullName = (p.name || 'Anonymous');
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
