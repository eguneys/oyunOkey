const buffer = require('vinyl-buffer');
const source = require('vinyl-source-stream');
const colors = require('ansi-colors');
const logger = require('fancy-log');
const watchify = require('watchify');
const browserify = require('browserify');
const uglify = require('gulp-uglify');
const size = require('gulp-size');


module.exports = (gulp, standalone, fileBaseName, dir) => {

  browserifyOpts = (debug) => ({
    entries: [`${dir}/src/main.js`],
    standalone: standalone,
    debug: debug
  });

  const destination = () => gulp.dest('../../public/compiled/');

  const prod = () => browserify(browserifyOpts(false))
        .transform('babelify',
                   { presets: ["@babel/preset-env"],
                     plugins: ['add-module-exports'] })
        .bundle()
        .pipe(source(`${fileBaseName}.min.js`))
        .pipe(buffer())
        .pipe(uglify())
        .pipe(size())
        .pipe(destination());

  const dev = () => browserify(browserifyOpts(true))
        .transform('babelify',
                   { presets: ["@babel/preset-env"] })
        .bundle()
        .pipe(source(`${fileBaseName}.js`))
        .pipe(destination());

  const watch = () => {
    const bundle = () => bundler
          .transform('babelify',
                     { presets: ["@babel/preset-env"],
                       plugins: [] })
          .bundle()
          .on('error', error => logger.error(colors.red(error.message)))
          .pipe(source(`${fileBaseName}.js`))
          .pipe(destination());

    const bundler = watchify(
      browserify(Object.assign({}, watchify.args, browserifyOpts(true)))
    )
          .on('update', bundle)
          .on('log', logger.info);
    return bundle();
  };

  gulp.task('prod', prod);
  gulp.task('dev', dev);
  gulp.task('default', watch);
}
