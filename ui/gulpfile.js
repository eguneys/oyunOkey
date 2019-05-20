const gulp = require('gulp');
const sass = require('gulp-sass');
const sourcemaps = require('gulp-sourcemaps');
const autoprefixer = require('gulp-autoprefixer');
const sassInheritance = require('gulp-sass-inheritance');
const rename = require('gulp-rename');
const cached = require('gulp-cached');
const gulpif = require('gulp-if');
const filter = require('gulp-filter');
const fs = require('fs');
const glob = require('glob');
const touch = require('gulp-touch-cmd');

const themes = ['light'];

const sassOptions = {
  errLogToConsole: true,
  outputStyle: 'expanded'
};

const autoprefixerOpts = {
  browsers: 'last 5 versions, Firefox ESR, not IE < 12, not < 0.1%, not IE_Mob < 12, not android < 4.4'.split(', ')
};

const destination = () => gulp.dest('../public/css/');

const sourcesGlob = './*/css/**/*.scss';
const buildsGlob = './*/css/build/*.scss';

const build = () => gulp.src(sourcesGlob)
      .pipe(gulpif(global.isWatching, cached('sass')))
      .pipe(sassInheritance({ dir: '.', debug: false }))
      .pipe(filter(file => !/\/_/.test(file.path) || !/^_/.test(file.relative)))
      .pipe(sourcemaps.init())
      .pipe(sass(sassOptions).on('error', sass.logError))
      .pipe(sourcemaps.write())
      .pipe(renameAs('dev'))
      .pipe(destination())
      .pipe(touch());

const setWatching = async () => { global.isWatching = true; };

const startWatching = () => gulp.watch(sourcesGlob, build);

gulp.task('css', gulp.series([
  createThemedBuilds,
  setWatching,
  build,
  startWatching
]));

gulp.task('css-dev', gulp.series([createThemedBuilds, build]));

gulp.task('css-prod', () => gulp
          .src(sourcesGlob)
          .pipe(sass({
            ...sassOptions,
            ...{ outputStyle: 'compressed' }
          }).on('error', sass.logError))
          .pipe(autoprefixer(autoprefixerOpts))
          .pipe(renameAs('min'))
          .pipe(destination())
         );

function renameAs(ext) {
  return rename(path => {
    path.dirname = '';
    path.basename = `${path.basename}.${ext}`;
    return path;
  });
}

function createThemedBuilds(cb) {
  glob(buildsGlob, {}, (err, files) => {
    files
      .filter(file => file.match(/\/_.+\.scss$/))
      .forEach(file => {
        themes.forEach(theme => {
          const themed = file.replace(/\/_(.+)\.scss$/, `/$1.${theme}.scss`);
          if (!fs.existsSync(themed)) {
            const buildName = file.replace(/.+\/_(.+)\.scss$/, '$1');
            const code = `@import '../../../common/css/theme/${theme}';\n@import '${buildName}';\n`;
            console.log(`Create missing SCSS themed build: ${themed}`);
            fs.writeFileSync(themed, code);
          }
        });
      });
    cb();
  });

}
