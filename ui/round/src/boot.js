export default function(opts) {
  const oy = oyunkeyf;

  const element = document.querySelector('.round__app'),
        data = opts.data;
  let round;

  if (data.masa) $('body').data('masa-id', data.masa.id);

  oy.socket = oy.StrongSocket(
    data.url.socket,
    data.player.version, {
      options: { name: 'round' },
      params: {},
      receive(t, d) { round.socketReceive(t, d); },
      events: { }      
    }
  );

  opts.element = element;
  opts.socketSend = oy.socket.send;
  round = (window['OyunkeyfRound']).app(opts);
}
