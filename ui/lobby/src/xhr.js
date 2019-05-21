export function anonPoolSeek(pool) {
  return $.ajax({
    method: 'POST',
    url: '/setup/hook/' + window.oyunkeyf.StrongSocket.sri,
    data: {
      variant: 1,
      rounds: pool.lim
    }
  });
}
