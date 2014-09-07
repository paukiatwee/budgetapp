var spinnerOpts = {
  lines: 13, // The number of lines to draw
  length: 20, // The length of each line
  width: 10, // The line thickness
  radius: 30, // The radius of the inner circle
  corners: 1, // Corner roundness (0..1)
  rotate: 0, // The rotation offset
  direction: 1, // 1: clockwise, -1: counterclockwise
  color: '#000', // #rgb or #rrggbb or array of colors
  speed: 1, // Rounds per second
  trail: 60, // Afterglow percentage
  shadow: false, // Whether to render a shadow
  hwaccel: false, // Whether to use hardware acceleration
  className: 'spinner', // The CSS class to assign to the spinner
  zIndex: 2e9, // The z-index (defaults to 2000000000)
  top: '50%', // Top position relative to parent
  left: '50%' // Left position relative to parent
};

var financeApp = angular.module('financeApp', ['ngRoute', 'ngAnimate', 'wu.masonry', 'ui.bootstrap', 'angular-google-analytics', 'angular-loading-bar', 'angularMoment', 'financeControllers', 'financeServices']);

angular.module('financeApp').constant('angularMomentConfig', {
  timezone: 'utc'
});

angular.module('financeApp').constant('financeAppConfig', {
  version: '1.0.0'
});

financeApp.config(['$routeProvider', '$httpProvider', '$locationProvider', 'AnalyticsProvider',
      function($routeProvider, $httpProvider, $locationProvider, AnalyticsProvider) {
        $httpProvider.interceptors.push('TokenInterceptor');
        var financeAppConfig = {version: '1.0.0'};

        // initial configuration
        AnalyticsProvider.setAccount('UA-53663138-1');

        // track all routes (or not)
        AnalyticsProvider.trackPages(true);

        //Optional set domain (Use 'none' for testing on localhost)
//        AnalyticsProvider.setDomainName('none');

        // Use analytics.js instead of ga.js
        AnalyticsProvider.useAnalytics(true);

        // change page event name
        AnalyticsProvider.setPageEvent('$routeChangeStart');

        $locationProvider.html5Mode(true);

        $routeProvider.
            when('/', {
              templateUrl: '/app/partials/login.html?' + financeAppConfig.version,
              controller: 'LoginController'
            }).
            when('/signup', {
              templateUrl: '/app/partials/signup.html?' + financeAppConfig.version,
              controller: 'SignupController'
            }).
            when('/dashboard', {
              templateUrl: '/app/partials/dashboard.html?' + financeAppConfig.version,
              controller: 'DashboardController'
            }).
            when('/manage', {
              templateUrl: '/app/partials/manage.html?' + financeAppConfig.version,
              controller: 'ManageController'
            }).
            when('/manage/:period', {
              templateUrl: '/app/partials/manage.html?' + financeAppConfig.version,
              controller: 'ManageController'
            }).
            when('/recurrings', {
              templateUrl: '/app/partials/recurrings.html?' + financeAppConfig.version,
              controller: 'RecurringController'
            }).
            when('/categories', {
              templateUrl: '/app/partials/categories.html?' + financeAppConfig.version,
              controller: 'CategoriesController'
            }).
            when('/categories/new', {
              templateUrl: '/app/partials/category.html?' + financeAppConfig.version,
              controller: 'CategoryController'
            }).
            when('/ledgers', {
              templateUrl: '/app/partials/ledgers.html?' + financeAppConfig.version,
              controller: 'LedgersController'
            }).
            when('/ledgers/new', {
              templateUrl: '/app/partials/ledger.html?' + financeAppConfig.version,
              controller: 'LedgerController'
            }).
            when('/profile', {
              templateUrl: '/app/partials/profile.html?' + financeAppConfig.version,
              controller: 'ProfileController'
            }).
            when('/reports', {
              templateUrl: '/app/partials/reports.html?' + financeAppConfig.version,
              controller: 'ReportsController'
            }).
            when('/help', {
              templateUrl: '/app/partials/help.html?' + financeAppConfig.version,
              controller: 'HelpController'
            }).
            when('/logout', {
              templateUrl: '/app/partials/blank.html?' + financeAppConfig.version,
              controller: 'LogoutController'
            }).
            when('/404', {
              templateUrl: '/app/partials/404.html?' + financeAppConfig.version,
              controller: 'NotFoundController'
            }).
            otherwise({
              redirectTo: '/404'
            });
      }]
);

financeApp.run(function($rootScope, $location, $window, Analytics, AuthenticationService, UserService) {
  $rootScope.$on("$routeChangeStart", function(event, nextRoute, currentRoute) {

    if(!$rootScope.user && AuthenticationService.anonymous.indexOf(nextRoute.originalPath) == -1) {
      UserService.ping().$promise.then(function(response) {
        $rootScope.user = response;
      });
    }

    if(nextRoute != null) {
      if(AuthenticationService.anonymous.indexOf(nextRoute.originalPath) == -1
          && !$window.sessionStorage.token) {
        delete $rootScope.user;
        $location.path("/");
      }
    }
  });
});

// filters
angular.module('ng').filter('truncate', function () {
  return function (value, wordwise, max, tail) {
    if (!value) return '';

    max = parseInt(max, 10);
    if (!max) return value;
    if (value.length <= max) return value;

    value = value.substr(0, max);
    if (wordwise) {
      var lastspace = value.lastIndexOf(' ');
      if (lastspace != -1) {
        value = value.substr(0, lastspace);
      }
    }

    return value + (tail || '…');
  };
});

// show N/A if the value is null or empty string
angular.module('ng').filter('na', function () {
  return function (value) {
    if (!value || value == '') return 'N/A';
    return value;
  };
});

// format date, format is 'YYYY-MM'
angular.module('ng').filter('yearMonth', ['moment', 'amMoment', function (moment, amMoment) {
  return function (value) {
    if (typeof value === 'undefined' || value === null) {
      return '';
    }

    value = amMoment.preprocessDate(value, null);
    var date = moment(value);
    if (!date.isValid()) {
      return '';
    }

    return amMoment.applyTimezone(date).format('YYYY-MM');
  };
}]);

// format date, format is 'YYYY-MM-DD'
angular.module('ng').filter('date', ['moment', 'amMoment', function (moment, amMoment) {
  return function (value) {
    if (typeof value === 'undefined' || value === null) {
      return '';
    }

    value = amMoment.preprocessDate(value, null);
    var date = moment(value);
    if (!date.isValid()) {
      return '';
    }

    return amMoment.applyTimezone(date).format('YYYY-MM-DD');
  };
}]);

// format datetime, format is 'YYYY-MM-DD HH:mm'
angular.module('ng').filter('datetime', ['moment', 'amMoment', function (moment, amMoment) {
  return function (value) {
    if (typeof value === 'undefined' || value === null) {
      return '';
    }

    value = amMoment.preprocessDate(value, null);
    var date = moment(value);
    if (!date.isValid()) {
      return '';
    }

    return amMoment.applyTimezone(date).format('YYYY-MM-DD HH:mm');
  };
}]);

angular.module('ng').directive('chart', function() {

  'use strict';

  return {
    restrict: 'E',
    link: function(scope, elem, attrs) {
      var options = scope[attrs.ngOptions] || {};
      scope.$watch(attrs.ngModel, function (data) {
        if (!data) { return; }
        $.plot(elem, data, options);
      }, true);
      elem.show();
    }
  };
});

angular.module('ng').directive('fooTable', function() {

  'use strict';

  return {
    restrict: 'A',
    link: function(scope, elem, attrs) {
      scope.$watch(attrs.ngModel, function (data) {
        if (!data) { return; }
        $(elem).footable();
      }, true);
      elem.show();
    }
  };
});

angular.module('ng').directive('bsNavbar', function($rootScope, $location) {
  'use strict';

  return {
    restrict: 'A',
    link: function postLink(scope, element, attrs, controller) {
      // Watch for the $location
      scope.$watch(function() {
        return $location.path();
      }, function(newValue, oldValue) {

        // show hide main nav
        $rootScope.nav = (newValue !== '/' && newValue !== '/signup');

        var links = element.find("a");
        for(var i = 0; i < links.length; i++) {
          var $link = angular.element(links[i]);
          var href = $link.attr('href');
          if(href === newValue) {
            $link.parent().addClass('active');
            var $treeview = $link.parent().parent().parent();
            if($treeview.hasClass("treeview")) {
              $treeview.addClass("active");
            }
          } else if($link.data("prefix") === true && newValue.indexOf(href) == 0) {
            $link.parent().addClass('active');
            var $treeview = $link.parent().parent().parent();
            if($treeview.hasClass("treeview")) {
              $treeview.addClass("active");
            }
          } else {
            $link.parent().removeClass('active');
          }
        }
      });
    }
  };
});
