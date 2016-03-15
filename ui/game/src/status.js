// scalaokey/src/main/scala/Status.scala

var ids = {
  created: 10,
  started: 20,
  aborted: 25,
  over: 30
};

function started(data) {
  return data.game.status.id >= ids.started;
}

function finished(data) {
  return data.game.status.id >= ids.over;
}

function aborted(data) {
  return data.game.status.id >= ids.aborted;
}


function playing(data) {
  return started(data) && !finished(data) && !aborted(data);
}

module.exports = {
  ids: ids,
  started: started,
  finished: finished,
  aborted: aborted,
  playing: playing
};
