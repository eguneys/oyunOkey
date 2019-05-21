const gulp = require('gulp');
const source = require('vinyl-source-stream');
const buffer = require('vinyl-buffer');
const colors = require('ansi-colors');
const logger = require('fancy-log');
const watchify = require('watchify');
const browserify = require('browserify');
const uglify = require('gulp-uglify');
const size = require('gulp-size');
const concat = require('gulp-concat');


const browserifyOpts = (entries, debug) => ({
  entries: entries,
  standalone: 'Oyunkeyf',
  debug: debug
});

const destinationPath = '../../public/compiled/';
const destination = () => gulp.dest(destinationPath);
const fileBaseName = 'oyunkeyf.site';

const prodSource = () => browserify(browserifyOpts('src/index.js', false))
      .transform('babelify',
                 { presets: ["@babel/preset-env"] })
      .bundle()
      .pipe(source(`${fileBaseName}.source.min.js`))
      .pipe(buffer())
      .pipe(uglify())
      .pipe(gulp.dest('./dist'));

const devSource = () => browserify(browserifyOpts('src/index.js', true))
      .transform('babelify',
                 { presets: ["@babel/preset-env"] })
      .bundle()
      .pipe(source(`${fileBaseName}.js`))
      .pipe(destination());

function makeDependencies(filename) {
  return function bundleDeps() {
    return gulp.src([
      '../../public/javascripts/vendor/jquery.min.js'
    ])
      .pipe(concat(filename))
      .pipe(destination());
  };  
}

function makeBundle(filename) {
  return function bundleItAll() {
    return gulp.src([
      destinationPath + 'oyunkeyf.deps.js',
      './dist/' + filename
    ])
      .pipe(concat(filename.replace('source.', '')))
      .pipe(destination());
  };
}


const deps = makeDependencies('oyunkeyf.deps.js');

const tasks = [deps];

const dev = gulp.series(tasks.concat([devSource]));

gulp.task('prod', gulp.series(tasks, prodSource, makeBundle(`${fileBaseName}.source.min.js`)));
gulp.task('dev', gulp.series(tasks, dev));
gulp.task('default', gulp.series(tasks, dev, () => gulp.watch('src/**/*.js', dev)));
