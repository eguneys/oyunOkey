var gulp = require('gulp');
var csso = require('gulp-csso');

gulp.task('default', function() {
  return gulp.src('./public/stylesheets/**/*.css')
    .pipe(csso())
    .pipe(gulp.dest('./public/compiled'));
});
