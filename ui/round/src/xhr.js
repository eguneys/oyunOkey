export const headers = {
  'Accept': 'application/vnd.oyunkeyf.v1+json'
};

export function reload(ctrl) {
  return $.ajax({
    url: ctrl.data.url.round,
    headers
  }).fail(window.oyunkeyf.reload);
}
