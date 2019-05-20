oyunkeyf.trans = function(i18n) {
  var format = function(str, args) {
    args.forEach(function(arg) {
      str = str.replace('%s', arg);
    });
    return str;
  };
  
  var trans = function(key) {
    var str = i18n[key];
    return str ? format(str, Array.prototype.slice.call(arguments, 1)) : key;  
  };
  
  return trans;
};
