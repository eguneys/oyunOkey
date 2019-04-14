var source = require('vinyl-source-stream');
// var gulp = require('gulp');
var tap = require('gulp-tap');
var gutil = require('gulp-util');
var jshint = require('gulp-jshint');
var watchify = require('watchify');
var browserify = require('browserify');
var uglify = require('gulp-uglify');
var streamify = require('gulp-streamify');

var log = console.log;

module.exports = (gulp, standalone, fileBaseName, dir) => {
  var sources = ['./src/main.js'];
  var destination = '../../public/compiled/';

  var onError = function(error) {
    log(error.message);
  };

  browserifyOpts = (debug) => ({
    entries: [`${dir}/src/main.js`],
    standalone: standalone,
    debug: debug
  });

  gulp.task('dev', function() {
    return browserify(browserifyOpts(true))
      .transform('babelify',
                 { presets: ["@babel/preset-env"],
                   plugins: ['add-module-exports'] })
      .bundle()
      .on('error', onError)
      .pipe(source(`${fileBaseName}.js`))
      .pipe(streamify(uglify()))
      .pipe(gulp.dest(destination));
  });

  gulp.task('watch', function() {
    var bundleStream = watchify(
      browserify(Object.assign({}, watchify.args, browserifyOpts(true)))
    )
        .transform('babelify',
                   { presets: ["@babel/preset-env"],
                     plugins: [] })
        .on('update', rebundle)
        .on('log', gutil.log);

    function rebundle() {
      return bundleStream.bundle()
        .on('error', onError)
        .pipe(source(`${fileBaseName}.js`))
        .pipe(gulp.dest(destination));
    }

    return rebundle();
  });

  gulp.task('default', ['watch']);
}
