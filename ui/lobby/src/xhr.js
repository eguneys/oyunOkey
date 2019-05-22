export function anonPoolSeek(pool) {
  // return $.ajax({
  //   method: 'POST',
  //   url: '/setup/hook/' + window.oyunkeyf.StrongSocket.sri,
  //   data: {
  //     variant: pool.var,
  //     rounds: pool.lim
  //   }
  // });
  var url = '/setup/hook/' + window.oyunkeyf.StrongSocket.sri;
  var $form = $(`<form style="display: none;" method="post" action="${url}"></form>`);
  $form.append(`<input name="rounds" value="${pool.lim}">`);
  $form.append(`<input name="variant" value="${pool.var}">`);
  $('body').append($form);
  $form.submit();
}

export function joinHook(id) {
  var url = `/masa/${id}/join`;
  var $form = $(`<form style="display: none;" method="post" action="${url}"></form>`);
  $('body').append($form);
  $form.submit(); 
}
