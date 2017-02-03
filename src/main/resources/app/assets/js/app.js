
var budgetApp = angular.module('budgetApp', ['ngRoute', 'ngAnimate', 'wu.masonry', 'ui.bootstrap', 'angular-loading-bar', 'angularMoment', 'financeControllers', 'financeServices']);

angular.module('budgetApp').constant('angularMomentConfig', {
  timezone: 'utc'
});

angular.module('budgetApp').constant('appConfig', {
  version: '1.0.4',
  analyticsAccount: 'UA-53663138-1'
});

function configureAnalytics(AnalyticsProvider, analyticsAccount) {
      // initial configuration
      AnalyticsProvider.setAccount(analyticsAccount);

      // track all routes (or not)
      AnalyticsProvider.trackPages(true);

      //Optional set domain (Use 'none' for testing on localhost)
      //        AnalyticsProvider.setDomainName('none');

      // Use analytics.js instead of ga.js
      AnalyticsProvider.useAnalytics(true);

      // change page event name
      AnalyticsProvider.setPageEvent('$routeChangeStart');
}

budgetApp.config(['$routeProvider', '$httpProvider', '$locationProvider', '$injector', 'appConfig',
      function($routeProvider, $httpProvider, $locationProvider, $injector, appConfig) {
        $httpProvider.interceptors.push('TokenInterceptor');

        try {
          AnalyticsProvider = $injector.get('angular-google-analytics');

          // have google analytics
          configureAnalytics(AnalyticsProvider, appConfig.analyticsAccount);
        } catch(e) {
            // no analytics, no sweat
          console.log("No Google Analytics available. Ignoring.");
        }

        $locationProvider.html5Mode({
          enabled: true,
          requireBase: false
        });

        $routeProvider.
            when('/', {
              templateUrl: '/app/partials/login.html?' + appConfig.version,
              controller: 'LoginController'
            }).
            when('/signup', {
              templateUrl: '/app/partials/signup.html?' + appConfig.version,
              controller: 'SignupController'
            }).
            when('/dashboard', {
              templateUrl: '/app/partials/dashboard.html?' + appConfig.version,
              controller: 'DashboardController'
            }).
            when('/dashboard/:period', {
              templateUrl: '/app/partials/dashboard.html?' + appConfig.version,
              controller: 'DashboardController'
            }).
            when('/manage', {
              templateUrl: '/app/partials/manage.html?' + appConfig.version,
              controller: 'ManageController'
            }).
            when('/manage/:period', {
              templateUrl: '/app/partials/manage.html?' + appConfig.version,
              controller: 'ManageController'
            }).
            when('/recurrings', {
              templateUrl: '/app/partials/recurrings.html?' + appConfig.version,
              controller: 'RecurringsController'
            }).
            when('/recurrings/new', {
              templateUrl: '/app/partials/recurring.html?' + appConfig.version,
              controller: 'RecurringController'
            }).
            when('/categories', {
              templateUrl: '/app/partials/categories.html?' + appConfig.version,
              controller: 'CategoriesController'
            }).
            when('/categories/new', {
              templateUrl: '/app/partials/category.html?' + appConfig.version,
              controller: 'CategoryController'
            }).
            when('/budgets', {
              templateUrl: '/app/partials/budgets.html?' + appConfig.version,
              controller: 'BudgetsController'
            }).
            when('/budgets/new', {
              templateUrl: '/app/partials/budget.html?' + appConfig.version,
              controller: 'BudgetController'
            }).
            when('/profile', {
              templateUrl: '/app/partials/profile.html?' + appConfig.version,
              controller: 'ProfileController'
            }).
            when('/reports', {
              templateUrl: '/app/partials/reports.html?' + appConfig.version,
              controller: 'ReportsController'
            }).
            when('/help', {
              templateUrl: '/app/partials/help.html?' + appConfig.version,
              controller: 'HelpController'
            }).
            when('/logout', {
              templateUrl: '/app/partials/blank.html?' + appConfig.version,
              controller: 'LogoutController'
            }).
            when('/404', {
              templateUrl: '/app/partials/404.html?' + appConfig.version,
              controller: 'NotFoundController'
            }).
            otherwise({
              redirectTo: '/404'
            });
      }]
);

budgetApp.run(function($rootScope, $location, $window, AuthenticationService, UserService) {
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
        $location.url("/");
      }
    }
  });
});

// filters
angular.module('budgetApp').filter('truncate', function () {
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
angular.module('budgetApp').filter('na', function () {
  return function (value) {
    if (!value || value == '') return 'N/A';
    return value;
  };
});

angular.module('budgetApp').filter('safeCurrency', ['$filter', function ($filter) {
  return function (value, symbol) {
    if (!symbol) {
      return $filter('currency')(value);
    } else {
      return $filter('currency')(value, symbol);
    }
  };
}]);

// format date, format is 'YYYY-MM'
angular.module('budgetApp').filter('yearMonth', ['moment', 'amMoment', function (moment, amMoment) {
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
angular.module('budgetApp').filter('date', ['moment', 'amMoment', function (moment, amMoment) {
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
angular.module('budgetApp').filter('datetime', ['moment', 'amMoment', function (moment, amMoment) {
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

// true ✔ otherwise ✘
angular.module('budgetApp').filter('tick', function () {
  return function (value) {
    return value? '✔': '✘';
  };
});

function showTooltip(x, y, contents) {

  var tooltip = $('<div id="tooltip">' + contents + '</div>').css( {
    position: 'absolute',
    visibility: 'none',
    top: y - 30,
    left: x - 20,
    border: '1px solid #fdd',
    padding: '2px',
    'background-color': '#fee'
  }).appendTo("body").fadeIn(200);
  // re position to center
  tooltip.css("left", x - tooltip.width() / 2);
}

angular.module('budgetApp').directive('chart', function() {

  'use strict';

  return {
    restrict: 'E',
    link: function(scope, elem, attrs) {
      var options = scope[attrs.chartOptions] || {};
      scope.$watch(attrs.ngModel, function (data) {
        if (!data) { return; }
        $.plot(elem, data, options);
        if(options.grid && options.grid.hoverable) {

          var previousPoint = null;

          $(elem).bind("plothover", function (event, pos, item) {
            if (item) {
              if (previousPoint != item.datapoint) {
                previousPoint = item.datapoint;

                $("#tooltip").remove();
                var x = item.datapoint[1].toFixed(2);

                showTooltip(item.pageX, item.pageY, "$" + x);
              }
            } else {
              $("#tooltip").remove();
              previousPoint = null;
            }
          });
        }
      }, true);
      elem.show();
    }
  };
});

angular.module('budgetApp').directive('fooTable', function() {

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

angular.module('budgetApp').directive('bsNavbar', function($rootScope, $location) {
  'use strict';

  return {
    restrict: 'A',
    link: function postLink(scope, element, attrs, controller) {
      // Watch for the $location
      scope.$watch(function() {
        return $location.url();
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

angular.module('budgetApp').filter('icon', function() {
  'use strict';

  var icons = [
    {
      icon: 'money',
      keywords: ['money', 'income', 'salary', 'bonus', 'wages', 'dividend', 'saving']
    },
    {
      icon: 'home',
      keywords: ['home', 'house']
    },
    {
      icon: 'shopping-cart',
      keywords: ['daily', 'living', 'shopping']
    },
    {
      icon: 'bus',
      keywords: ['bus', 'transportation']
    },
    {
      icon: 'taxi',
      keywords: ['taxi', 'cab', 'automobile', 'car', 'transportation']
    },
    {
      icon: 'hospital',
      keywords: ['hospital', 'medical']
    },
    {
      icon: 'loan',
      keywords: ['loan', 'university']
    },
    {
      icon: 'user-md',
      keywords: ['doctor', 'medical', 'health']
    },
    {
      icon: 'car',
      keywords: ['car']
    },
    {
      icon: 'child',
      keywords: ['child']
    },
    {
      icon: 'gift',
      keywords: ['gift']
    },
    {
      icon: 'gamepad',
      keywords: ['game', 'entertainment']
    },
    {
      icon: 'credit-card',
      keywords: ['obligation']
    }
  ];

  return function (name) {
    var icon = _.find(icons, function(item) {
      var names = name.toLowerCase().replace('/', ' ').split(' ');
      for (var index = 0; index < names.length; ++index) {
        var n = names[index];
        if(endsWith(n, 's')) {
          n = n.substring(0, n.length - 1)
        }

        if (item.keywords.indexOf(n) != -1) {
          return true;
        }
      }
    });

    if(icon) {
      return '<span class="fa fa-lg fa-' + icon.icon + '"></span> ' + name;
    } else {
      return ' <span class="fa fa-lg fa-usd"></span> ' + name;
    }

  };
});


angular.module('budgetApp').filter('unsafe', function($sce) {
  return function(val) {
    return $sce.trustAsHtml(val);
  };
});


function endsWith(str, suffix) {
  return str.indexOf(suffix, str.length - suffix.length) !== -1;
}
