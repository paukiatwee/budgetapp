var less = require('gulp-less'),
    cssmin = require('gulp-minify-css'),
    gulp = require('gulp');

gulp.task('default', function() {
  // place code for your default task here
});

var paths = {
  run: 'src/run.js',
  watch: {
    files: ['src/main/resources/app/assets/less/*.less']
  },
  less: {
    files: ['src/main/resources/app/assets/less/style.less'],
    root: 'src/main/resources/app/assets/less'
  },
  css: {
    files: ['src/main/resources/app/assets/css/style.css'],
    root: 'src/main/resources/app/assets/css'
  },
  dest: 'src/main/resources/app/assets/css'
};

// compile LESS
gulp.task('compile-less', function() {
  return gulp.src(paths.less.files)
      .pipe(less())
      .pipe(gulp.dest(paths.dest));
});

// concat and minify CSS files
gulp.task('minify-css', function() {
  return gulp.src(paths.css.files)
      .pipe(cssmin({root:paths.css.root}))
      .pipe(gulp.dest(paths.dest));
});

// watch task
gulp.task('watch', function () {
  gulp.watch(paths.watch.files, ['build']);
});

gulp.task('build', ['compile-less', 'minify-css'], function(){ });

