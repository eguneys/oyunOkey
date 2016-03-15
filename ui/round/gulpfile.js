var source = require('vinyl-source-stream');
var gulp = require('gulp');
var tap = require('gulp-tap');
//var gutil = require('gulp-util');
var jshint = require('gulp-jshint');
var watchify = require('watchify');
var browserify = require('browserify');
var uglify = require('gulp-uglify');
var streamify = require('gulp-streamify');

var log = console.log;

var sources = ['./src/main.js'];
var destination = '../../public/compiled';
var onError = function(error) {
  log(error.message);
};

var standalone = 'OyunkeyfRound';

gulp.task('lint', function() {
  return gulp.src('./src/main.js')
    .pipe(jshint())
    .pipe(jshint.reporter('default'));
});

gulp.task('dev', function() {
  return browserify('./src/main.js', {
    standalone: standalone
  }).transform('babelify',
               { presets: ["es2015"],
                 plugins: ['add-module-exports'] })
    .bundle()
    .on('error', onError)
    .pipe(source('oyunkeyf.round.js'))
    .pipe(streamify(uglify()))
    .pipe(gulp.dest(destination));
});

gulp.task('watch', function() {
  var opts = watchify.args;
  opts.debug = true;
  opts.standalone = standalone;
  var bundleStream = watchify(browserify(sources, opts))
    .transform('babelify',
               { presets: ["es2015"],
                 plugins: ['add-module-exports'] })
    .on('update', rebundle)
    .on('log', log);

  function rebundle() {
    return bundleStream.bundle()
      .on('error', onError)
      .pipe(source('oyunkeyf.round.js'))
      .pipe(gulp.dest(destination));
  }

  return rebundle();
});

gulp.task('default', ['watch']);
