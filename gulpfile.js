const { src, dest, series, watch } = require('gulp');
const less = require('gulp-less');
const cssmin = require('gulp-minify-css');

function defaultTask(cb) {
  // place code for your default task here
  cb();
}

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
function compileLess() {
  return src(paths.css.files)
      .pipe(less({root: paths.css.root}))
      .pipe(dest(paths.dest));
}

// concat and minify CSS files
function minifyCss() {
  return src(paths.css.files)
      .pipe(cssmin({root: paths.css.root}))
      .pipe(dest(paths.dest));
}

// watch task
function watchFiles() {
  watch(paths.watch.files, series(compileLess))
}

exports.default = defaultTask
exports.compileLess = compileLess
exports.minifyCss = minifyCss
exports.watch = watchFiles
exports.build = series(compileLess)
