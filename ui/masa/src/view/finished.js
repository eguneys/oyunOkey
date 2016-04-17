import m from 'mithril';
import header from './header';
import pagination from '../pagination';
import { podium, standing } from './arena';


module.exports = {
  main: function(ctrl) {
    var pag = pagination.players(ctrl);
    return [
      header(ctrl),
      podium(ctrl),
      standing(ctrl, pag)
    ];
  }
};
