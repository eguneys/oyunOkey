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
var destination = './build';
var onError = function(error) {
  log(error.message);
};

var standalone = 'OyunkeyfGame';

gulp.task('dev', function() {
  return browserify('./src/main.js', {
    standalone: standalone
  }).transform('babelify',
               { presets: ["es2015"],
                 plugins: ['add-module-exports'] })
    .bundle()
    .on('error', onError)
    .pipe(source('oyunkeyf.game.min.js'))
    .pipe(streamify(uglify()))
    .pipe(gulp.dest(destination));
});

gulp.task('default', ['dev']);
