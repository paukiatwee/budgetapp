
var financeControllers = angular.module('financeControllers', []);

financeControllers.controller('LoginController', function ($scope, $rootScope, $window, AuthenticationService, $http, $location, auth) {

  $scope.login = function() {
    if(!$scope.user || !$scope.user.username || !$scope.user.password) {
      $scope.user = {username: "", password: ""};
    }
    $http({
      url: "/api/users/auth",
      method: "POST",
      data: angular.toJson($scope.user)
    }).then(function(response) {
          // success
          auth.setCredentials($scope.user.username, $scope.user.password);
          AuthenticationService.isLogged = true;
          $window.sessionStorage.token = response.data.token;
          $rootScope.user = response.data;
          $location.path("/dashboard");
        },
        function(response) { // optional
          // failed
          $scope.error = true;
          if(response.status === 401) {
            $scope.response = {errors:  {"username": ["Invalid username and password combination"]}};
          } else {
            $scope.response = response.data;
          }
        }
    );
  };
});

financeControllers.controller('SignupController', function ($scope, $rootScope, $location, UserService) {

  $scope.errorMessage = errorMessage;
  $scope.errorClass = errorClass;


  $scope.register = function() {
    $scope.error = false;
    $scope.message = null;
    UserService.signup($scope.user || {}).$promise.then(function() {
      $location.path("/");
    }, function(response) {
      failure($scope, response)
    });
  };
});

financeControllers.controller('DashboardController', function ($scope, $modal, LedgerService,
                                                               CategoryService, TransactionService, UserService) {
  $scope.usageLoaded = false;
  $scope.usage = UserService.usage(function() {
    $scope.usageLoaded = true;
  });
  $scope.transactionLoaded = false;
  $scope.transactionsOptions = {
    series: {
      lines: {
        show: true,
        fill: true,
        fillColor: {
          colors: [{opacity: 0}, {opacity: .3}]
        }
      },
      points: {
        show: !0,
        lineWidth: 2,
        fill: !0,
        fillColor: "#ffffff",
        symbol: "circle",
        radius: 5
      }
    },
    colors: ["#31C0BE"],
    grid: {
      hoverable: true,
      clickable: true,
      tickColor: "#f9f9f9"
    },
    xaxis: {
      mode: "time",
      timeformat: "%d-%b",
      tickSize: [1, "day"]
    },
    yaxis: {
      min: 0
    }
  };

  TransactionService.summary(function (response) {
    $scope.transactionsData = [_.map(response, function(point) {return [point.key, point.value]})];
  });


  $scope.categoriesOptions = {
    series: {
      pie: {
        show: true,
        radius: 1,
        label: {
          show: true,
          radius: 3/4,
          formatter: labelFormatter,
          background: {
            opacity: 0.5
          }
        }
      }
    },
    colors: ["#60CD9B", "#66B5D7", "#EEC95A", "#E87352"]
  };

  CategoryService.summary(function (response) {
    if(response.length == 0) {
      $scope.categoriesData = [{label: "No Data", data: 1}];
    } else {
      $scope.categoriesData = _.map(response, function(point) {return {label: point.label, data: point.value}});
    }
  });

  $scope.monthlyOptions = {
    series: {
      bars: {
        show: true,
        barWidth: 12*24*60*60*800,
        order: 1,
        fill: 0.9
      }
    },
    xaxis: {
      mode: "time",
      tickLength: 0,
      tickSize: [1, "month"],
      axisLabel: 'Month'
    },
    yaxis: {
      min: 0
    },
    grid: {
      hoverable: true,
      borderWidth: 0
    },
    colors: ["#60CD9B", "#66B5D7", "#EEC95A", "#E87352"]
  };

  TransactionService.monthly(function (response) {
    var spendings =
        _.chain(response)
            .filter(function(point){ return point.pointType == 'MONTHLY_SPEND'})
            .map(function(point){ return [point.key, point.value]})
            .value();
    var budgets =
        _.chain(response)
            .filter(function(point){ return point.pointType == 'MONTHLY_BUDGET'})
            .map(function(point){ return [point.key, point.value]})
            .value();

    $scope.monthlyData = [
      {label: "Budget", data: budgets},
      {label: "Spending", data: spendings}
    ];
  });
});

financeControllers.controller('ProfileController', function ($scope, UserService) {

  $scope.loaded = false;
  $scope.errorMessage = errorMessage;
  $scope.errorClass = errorClass;

  $scope.user = UserService.ping(function() {
    $scope.loaded = true;
  });

  $scope.updateProfile = function() {
    var user = $scope.user || {};
    UserService.update(angular.toJson(user), function() {
      // ok
      $scope.success = true;
      $scope.message = "Successfully update profile";
    }, function() {
      // error

    })
  };

});

function labelFormatter(label, series) {
  return "<div style='font-size:8pt; text-align:center; padding:2px; color:white;'>" + Math.round(series.percent) + "%</div>";
}

financeControllers.controller('ManageController', function ($scope, $routeParams, $modal, LedgerService, TransactionService, UserService) {

  $scope.loaded = false;
  $scope.errorMessage = errorMessage;
  $scope.errorClass = errorClass;


  var groups = [];

  var current = moment(new Date()).format("YYYY-MM");
  var period = $routeParams.period || current;
  var year = period.split("-")[0];
  var month = period.split("-")[1];


  // usage
  $scope.usageLoaded = false;
  $scope.usage = UserService.usage({month: month, year: year}, function() {
    $scope.usageLoaded = true;
  });


  for(var i = -1; i < 2; i++) {
    var now = moment(period, "YYYY-MM").toDate();
    var date = now.setMonth(now.getMonth() + i);
    var path = moment(date).format("YYYY-MM");
    groups.push({
      date: date,
      display: moment(date).format("MMM YYYY"),
      path: path,
      active: path == period,
      current: path == current
    });
  }

  $scope.groups = groups;
  $scope.summary = UserService.account({month: month, year: year}, function() {
    $scope.loaded = true;
  });

  $scope.$watch(
      "summary",
      function(summary) {
        var totalSpent = 0.0;
        var totalBudget = 0.0;
        var  changed = false;
        _.forEach(summary.groups, function(group) {
          var spent = _.reduce(group.ledgers, function(sum, ledger) {
            changed = true;
            ledger.remaining = ledger.budget - ledger.spent;

            if(group.type == 'EXPENSE') {
              return sum + ledger.spent;
            } else {
              return sum;
            }

          }, 0);
          totalSpent += spent;
          group.spent = spent;
        });

        _.forEach(summary.groups, function(group) {
          var budget = _.reduce(group.ledgers, function(sum, ledger) {
            changed = true;

            if(group.type == 'EXPENSE') {
              return sum + ledger.budget;
            } else {
              return sum;
            }
          }, 0);
          totalBudget += budget;
          group.budget = budget;
          group.remaining = group.budget - group.spent;
        });

        if(changed) {
          $scope.usage.spent = totalSpent;
          $scope.usage.budget = totalBudget;
          $scope.usage.remaining = totalBudget - totalSpent;
        }
      },
      true
  );

  $scope.openLedgerModal = function (ledger) {
    var modalInstance = $modal.open({
      templateUrl: 'ledgerModal.html',
      controller: LedgerModalController,
      resolve: {
        ledger: function() {
          return ledger;
        }
      }
    });

    modalInstance.result.then(function (selected) {
      LedgerService.update(selected);
    }, function () {

    });
  };

  $scope.openTransactionModal = function (ledger) {
    var modalInstance = $modal.open({
      templateUrl: 'transactionModal.html',
      controller: TransactionModalController,
      resolve: {
        ledger: function() {
          return ledger;
        }
      }
    });
  };

  $scope.openTransactionsModal = function (ledger) {
    $scope.transactionsLoaded = false;
    $modal.open({
      templateUrl: '/app/partials/transactions.html',
      scope: $scope,
      controller: TransactionsModalController,
      size: 'lg',
      resolve: {
        transactions: function() {
          return LedgerService.query({id: ledger.id, transactions: 'transactions'}, function() {
            $scope.transactionsLoaded = true;
          });
        },
        ledger: function() {
          return ledger;
        }
      }
    });
  };

  $scope.changeGroup = function(group) {

//    _.forEach($scope.data.groups, function(g) {g.active = false});
//    _.find($scope.data.groups, function(g) {
//      return g.date == group.date
//    }).active = true;
//
//    $scope.data.loaded = false;
//    var selectedDate = new Date(group.date);
//    var summaries = UserService.account({month: selectedDate.getMonth() + 1, year: selectedDate.getFullYear()}, function() {
//      $scope.data.loaded = true;
//    });
//    $scope.data.summary = summaries;
  }
});


var LedgerModalController = function ($scope, $modalInstance, ledger) {

  $scope.data = {};
  $scope.selected = ledger;
  $scope.original = angular.copy(ledger);

  $scope.ok = function () {
    if(!$scope.selected.budget) {
      $scope.selected.budget = 0;
    }
    $modalInstance.close($scope.selected);
  };

  $scope.cancel = function () {
    $scope.selected.name = $scope.original.name;
    $scope.selected.budget = $scope.original.budget;
    $modalInstance.dismiss('cancel');
  };
};

var TransactionModalController = function ($scope, $modalInstance, ledger, TransactionService) {

  $scope.selected = ledger;
  $scope.original = angular.copy(ledger);
  $scope.transaction = {ledger: {id: ledger.id}, recurringType: 'DAILY'};

  $scope.errorMessage = errorMessage;
  $scope.errorClass = errorClass;

  // TODO https://github.com/angular-ui/bootstrap/issues/969
  $scope.ok = function (form) {
    TransactionService.save($scope.transaction).$promise.then(function() {
      ledger.spent += $scope.transaction.amount;
      $modalInstance.close();
    }, function(response) {
      $scope.form = form;
      failure($scope, response);
    });

  };

  $scope.cancel = function () {
    $scope.selected = $scope.original;
    $modalInstance.dismiss('cancel');
  };
};

var TransactionsModalController = function ($scope, $modalInstance, ledger, transactions) {

  $scope.selected = ledger;
  $scope.transactions = transactions;
  $scope.cancel = function () {
    $modalInstance.dismiss('cancel');
  };
};

financeControllers.controller('LedgerController', function ($scope, CategoryService, LedgerService) {

  // cache categories, reuse when validation failed.
  var categories = null;
  $scope.loaded = false;
  $scope.ledger = {};

  $scope.errorMessage = errorMessage;
  $scope.errorClass = errorClass;

  CategoryService.query(function(response) {
    categories = response;
    $scope.categories = response;
    $scope.loaded = true;
  });

  $scope.createLedger = function() {
    $scope.success = false;
    $scope.error = false;
    $scope.message = null;
    var ledger = $scope.ledger;
    if(ledger.category) {
      ledger.categoryId = ledger.category.id;
    } else {
      ledger.categoryId = null;
    }
    LedgerService.save($scope.ledger).$promise.then(function() {
      $scope.success = true;
      $scope.message = "Successfully created Ledger";
      $scope.ledger = {};
      success($scope);
      // repopulate categories
      $scope.categories = categories;
    }, function(response) {
      failure($scope, response);
      // repopulate categories
      $scope.categories = categories;
    });
  }
});

financeControllers.controller('CategoryController', function ($scope, CategoryService) {

  $scope.errorMessage = errorMessage;
  $scope.errorClass = errorClass;

  $scope.createCategory = function() {
    $scope.success = false;
    $scope.error = false;
    $scope.message = null;
    CategoryService.save($scope.category || {}).$promise.then(function() {
      $scope.success = true;
      $scope.message = "Successfully created Category";
      $scope.category = {};
      clearErrors($scope);
    }, function(response) {
      failure($scope, response)
    });
  }
});

financeControllers.controller('RecurringsController', function ($scope, $modal, RecurringService) {

  $scope.loaded = false;
  $scope.recurrings = RecurringService.query(function() {
    $scope.loaded = true;
  });

  $scope.confirmDelete = function (recurring) {
    var modalInstance = $modal.open({
      templateUrl: '/app/partials/confirmDeleteModal.html',
      controller: ConfirmDeleteModalController,
      resolve: {
        modal: function() {
          return recurring;
        },
        message: function() {
          return ' this recurring';
        }
      }
    });

    modalInstance.result.then(function (selected) {
      selected.$delete({id: selected.id});
      $scope.recurrings.splice($scope.recurrings.indexOf(selected), 1);
    }, function () {

    });
  };

  $scope.openRecurringModal = function (recurring) {
    $scope.transactionsLoaded = false;

    $modal.open({
      templateUrl: '/app/partials/transactions.html',
      scope: $scope,
      controller: RecurringModalController,
      size: 'lg',
      resolve: {
        transactions: function() {
          return RecurringService.transactions({id: recurring.id, path: 'transactions'}, function() {
            $scope.transactionsLoaded = true;
          });
        },
        recurring: function() {
          return recurring;
        }
      }
    });
  };

});

financeControllers.controller('RecurringController', function ($scope, RecurringService, LedgerService) {


  // cache ledgers, reuse when validation failed.
  var ledgers = null;
  $scope.loaded = false;
  $scope.recurring = {};

  $scope.errorMessage = errorMessage;
  $scope.errorClass = errorClass;

  LedgerService.query(function(response) {
    ledgers = response;
    $scope.ledgers = response;
    $scope.loaded = true;
  });

  $scope.createRecurring = function() {
    $scope.success = false;
    $scope.error = false;
    $scope.message = null;
    var recurring = $scope.recurring;
    if(recurring.ledger) {
      recurring.ledgerId = recurring.ledger.id;
    } else {
      recurring.ledgerId = null;
    }
    RecurringService.save($scope.recurring).$promise.then(function() {
      $scope.success = true;
      $scope.message = "Successfully created Recurring";
      $scope.recurring = {};
      clearErrors($scope);
      // repopulate ledgers
      $scope.ledgers = ledgers;
    }, function(response) {
      failure($scope, response)
      // repopulate ledgers
      $scope.ledgers = ledgers;
    });
  }
});


financeControllers.controller('CategoriesController', function ($scope, $modal, CategoryService) {

  $scope.loaded = false;
  $scope.categories = CategoryService.query(function() {
    $scope.loaded = true;
  });

  $scope.confirmDelete = function (category) {
    var modalInstance = $modal.open({
      templateUrl: '/app/partials/confirmDeleteModal.html',
      controller: ConfirmDeleteModalController,
      resolve: {
        modal: function() {
          return category;
        },
        message: function() {
          return ' this category';
        }
      }
    });

    modalInstance.result.then(function (selected) {
      selected.$delete({id: selected.id}, function() {
        $scope.categories.splice($scope.categories.indexOf(selected), 1);
      }, function(response) {
        errorModal($modal, response.data.errors);
      });
    });
  };

  $scope.openLedgersModal = function (category) {
    $scope.ledgersLoaded = false;

    $modal.open({
      templateUrl: '/app/partials/ledgers.html',
      scope: $scope,
      controller: LedgersModalController,
      size: 'lg',
      resolve: {
        category: function() {
          return category;
        },
        ledgers: function() {
          return CategoryService.ledgers(category, function() {
            $scope.ledgersLoaded = true;
          });
        }
      }
    });
  };
});

financeControllers.controller('LedgersController', function ($scope, $modal, LedgerService) {

  $scope.loaded = false;
  $scope.ledgers = LedgerService.query(function() {
    $scope.loaded = true;
  });

  $scope.confirmDelete = function (ledger) {
    var modalInstance = $modal.open({
      templateUrl: '/app/partials/confirmDeleteModal.html',
      controller: ConfirmDeleteModalController,
      resolve: {
        modal: function() {
          return ledger;
        },
        message: function() {
          return ' this ledger';
        }
      }
    });

    modalInstance.result.then(function (selected) {
      selected.$delete({id: selected.id}, function() {
        $scope.ledgers.splice($scope.ledgers.indexOf(selected), 1);
      }, function(response) {
        errorModal($modal, response.data.errors);
      });
    });
  };
});

var LedgersModalController = function ($scope, $modalInstance, category, ledgers) {
  $scope.selected = category;
  $scope.ledgers = ledgers;
  $scope.cancel = function () {
    $modalInstance.dismiss('cancel');
  };
};

var RecurringModalController = function ($scope, $modalInstance, recurring, transactions) {

  $scope.selected = recurring;
  $scope.transactions = transactions;
  $scope.cancel = function () {
    $modalInstance.dismiss('cancel');
  };

};

financeControllers.controller('ReportsController', function ($scope, ReportService) {

  $scope.transactionsLoaded = false;
  ReportService.transactions(angular.toJson({}), function (response) {
    $scope.transactions = response;
    $scope.transactionsLoaded = true;
  })
  $scope.doSearch = function () {
    var search = $scope.search || {};
    $scope.transactionsLoaded = false;
    ReportService.transactions(angular.toJson(search), function (response) {
      $scope.transactions = response;
      $scope.transactionsLoaded = true;
    })
  };

});


financeControllers.controller('HelpController', function ($scope) {

});

financeControllers.controller('NotFoundController', function ($scope) {

});


financeControllers.controller('LogoutController', function ($scope, $rootScope, $location, $window, AuthenticationService, auth) {

  auth.clearCredentials();
  $location.path("/");

  $scope.logout = function() {
    auth.clearCredentials();
    AuthenticationService.isLogged = false;
    delete $window.sessionStorage.token;
    delete $rootScope.nav;
    delete $rootScope.user;
    delete $rootScope.username;
    $location.path("/");
  }
});


var ConfirmDeleteModalController = function ($scope, $modalInstance, modal, message) {

  $scope.selected = modal;
  $scope.original = angular.copy(modal);
  $scope.message = message;

  $scope.ok = function () {
    $modalInstance.close($scope.selected);
  };

  $scope.cancel = function () {
    $scope.selected = $scope.original;
    $modalInstance.dismiss('cancel');
  };
};

var ErrorModalController = function ($scope, $modalInstance, errors) {

  $scope.errors = errors;

  $scope.close = function () {
    $modalInstance.dismiss('cancel');
  };
};


// error modal
function errorModal($modal, errors) {
  $modal.open({
    templateUrl: '/app/partials/errorModal.html',
    controller: ErrorModalController,
    resolve: {
      errors: function() {
        return errors;
      }
    }
  });
}


// validations
function success($scope) {
  $scope.response = {};
  _.each($scope.form, function (field) {
    if (field.$setValidity) {
      field.$dirty = false;
      field.$setValidity("server", true);
    }
  });
};

function clearErrors($scope) {
  $scope.response = {};
  _.each($scope.form, function (field) {
    if(field.$setValidity) {
      field.$setValidity("server", true);
    }
  });
};

function failure($scope, response) {

  $scope.response = response.data;

  _.each($scope.form, function (field) {
    if(field.$setValidity) {
      field.$setValidity("server", true);
    }
  });
  _.each(response.data.errors, function(errors, key) {
    _.each(errors, function(e) {
      var field = $scope.form[key];
      field.$dirty = true;
      field.$setValidity("server", false);
    });
  });
};

function errorMessage (name) {
  var scope = this;
  if(!scope.response || !scope.response.errors) {
    return "";
  }
  var results = scope.response.errors[name];
  if(results) {
    return results.join(", ");
  }
  return "";
};

function errorClass(name) {
  var s = this.form[name];
  return s.$invalid && s.$dirty ? "has-error" : "";
};
