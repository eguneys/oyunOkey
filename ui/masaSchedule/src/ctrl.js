import m from 'mithril';

module.exports = function(env) {
  this.data = env.data;

  this.userId = env.userId;

  this.update = (data) => {
    this.data = data;
    m.redraw();
  };

  this.trans = oyunkeyf.trans(env.i18n);
  setInterval(m.redraw, 3700);
};
