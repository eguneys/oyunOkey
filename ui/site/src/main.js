(function() {
  $(function() {

    if (oyunkeyf.masa) startMasa(oyunkeyf.masa);

  });

  function startMasa(cfg) {
    var element = document.querySelector('main.masa');
    $('body').data('masa-id', cfg.data.id);
    var masa;
    oyunkeyf.socket = oyunkeyf.StrongSocket(
      '/masa/' + cfg.data.id + '/socket/v4', cfg.data.socketVersion, {
        receive: function(t, d) {
          return masa.socketReceive(t, d);
        }
      });
    cfg.socketSend = oyunkeyf.socket.send;
    cfg.element = element;
    cfg.$side = $('.masa__side').clone();
    cfg.$faq = $('.masa__faq').clone();
    masa = OyunkeyfMasa.start(cfg);
  }
})();
