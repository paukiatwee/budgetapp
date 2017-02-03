
var financeControllers = angular.module('financeControllers', []);

financeControllers.controller('LoginController', function ($scope, $rootScope, $window, $routeParams, AuthenticationService, $http, $location, auth) {

  $scope.success = $routeParams.signup || false;
  if($scope.success) {
    $scope.message = "Sign Up success. You can Login now.";
  }

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
          $location.url("/dashboard");
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
      $location.search('signup', 'true').path("/");
    }, function(response) {
      failure($scope, response)
    });
  };
});

financeControllers.controller('DashboardController', function ($scope, $modal, $location, $routeParams, BudgetService,
                                                               CategoryService, TransactionService, UserService) {
  $scope.usageLoaded = false;

  $scope.periods = [];

  for(var i = 0; i < 7; i++) {
    $scope.periods.push(moment().subtract(i, 'months').format("YYYY-MM"));
  }

  $scope.period = $routeParams.period || $scope.periods[0];

  $scope.reload = function() {
    $location.url("/dashboard/" + $scope.period);
  };

  var year = $scope.period.split("-")[0];
  var month = $scope.period.split("-")[1];

  $scope.usage = UserService.usage({month: month, year: year}, function() {
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

  TransactionService.summary({month: month, year: year}, function (response) {
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

  CategoryService.summary({month: month, year: year}, function (response) {
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

  TransactionService.todayRecurrings(function(response) {
    $scope.recurringTransactions = response;
  });

});

financeControllers.controller('ProfileController', function ($scope, UserService) {

  // method allow child controllers to access parent controller's scope
  $scope.updateSuccess = function(message) {
    $scope.success = true;
    $scope.message = message;
  };
});

financeControllers.controller('UpdateProfileController', function ($scope, $rootScope, UserService) {

  $scope.loaded = false;
  $scope.errorMessage = errorMessage;
  $scope.errorClass = errorClass;

  $scope.user = $rootScope.user = UserService.ping(function() {
    $scope.loaded = true;
  });

  $scope.updateProfile = function() {
    var user = $scope.user || {};
    UserService.update(angular.toJson(user), function() {
      // ok
      $scope.updateSuccess("Successfully update profile");
    }, function() {
      // error

    })
  };
});

financeControllers.controller('ChangePasswordController', function ($scope, UserService) {

  $scope.loaded = false;
  $scope.errorMessage = errorMessage;
  $scope.errorClass = errorClass;


  $scope.changePassword = function() {
    var password = $scope.password || {};
    UserService.changePassword(angular.toJson(password), function() {
      // ok
      $scope.updateSuccess("Successfully change password");
      $scope.password = {};
      clearErrors($scope);
    }, function(response) {
      // error
      failure($scope, response);
    })
  };
});

function labelFormatter(label, series) {
  return "<div style='font-size:8pt; text-align:center; padding:2px; color:white;'>" + Math.round(series.percent) + "%</div>";
}

financeControllers.controller('ManageController', function ($scope, $routeParams, $modal, appConfig, BudgetService, TransactionService, UserService) {

  $scope.loaded = false;
  $scope.errorMessage = errorMessage;
  $scope.errorClass = errorClass;


  var groups = [];

  var current = moment().format("YYYY-MM");
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
        var totalIncome = 0.0;
        var totalActual = 0.0;
        var totalProjected = 0.0;
        var  changed = false;
        _.forEach(summary.groups, function(group) {

          var projected = 0.0;
          var actual = 0.0;

          _.forEach(group.budgets, function (budget) {
            changed = true;

            if (group.type == 'INCOME') {
              // total actual income
              totalIncome += budget.actual;
            } else {
              // total spending budget
              totalProjected += budget.projected;
              // total actual spending
              totalActual += budget.actual;
            }

            budget.remaining = budget.projected - budget.actual;

            projected += budget.projected;
            actual += budget.actual;
          });

          group.projected = projected;
          group.actual = actual;
          group.remaining = group.projected - group.actual;
        });

        if(changed) {
          $scope.usage.income = totalIncome;
          $scope.usage.projected = totalProjected;
          $scope.usage.actual = totalActual;
          $scope.usage.remaining = totalProjected - totalActual;
        }
      },
      true
  );

  $scope.openBudgetModal = function (budget) {
    var modalInstance = $modal.open({
      templateUrl: 'budgetModal.html',
      controller: BudgetModalController,
      resolve: {
        budget: function() {
          return budget;
        }
      }
    });

    modalInstance.result.then(function (selected) {
      BudgetService.update(selected);
    }, function () {

    });
  };

  $scope.openTransactionModal = function (budget) {
    var modalInstance = $modal.open({
      templateUrl: 'transactionModal.html',
      controller: TransactionModalController,
      resolve: {
        budget: function() {
          return budget;
        }
      }
    });
  };

  $scope.openTransactionsModal = function (budget) {
    $scope.transactionsLoaded = false;
    $scope.transactionAction = true;
    $scope.deleteTransaction = function (transactions, transaction) {
      transaction.loading = true;
      TransactionService.delete({id: transaction.id}, function() {
        // update UI
        budget.actual = budget.actual - transaction.amount;
        transactions.splice(transactions.indexOf(transaction), 1);
      }, function(response) {
        errorModal($modal, response.data.errors);
      });
    };

    $modal.open({
      templateUrl: '/app/partials/transactions.html?' + appConfig.version,
      scope: $scope,
      controller: TransactionsModalController,
      size: 'lg',
      resolve: {
        transactions: function() {
          return BudgetService.query({id: budget.id, transactions: 'transactions'}, function() {
            $scope.transactionsLoaded = true;
          });
        },
        budget: function() {
          return budget;
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
  };

  TransactionService.todayRecurrings(function(response) {
    $scope.recurringTransactions = response;
  });
});


var BudgetModalController = function ($scope, $modalInstance, budget) {

  $scope.data = {};
  $scope.selected = budget;
  $scope.original = angular.copy(budget);

  $scope.ok = function () {
    if(!$scope.selected.projected) {
      $scope.selected.projected = 0;
    }

    if(!$scope.selected.actual) {
      $scope.selected.actual = 0;
    }
    $modalInstance.close($scope.selected);
  };

  $scope.cancel = function () {
    $scope.selected.name = $scope.original.name;
    $scope.selected.projected = $scope.original.projected;
    $scope.selected.actual = $scope.original.actual;
    $modalInstance.dismiss('cancel');
  };
};

var TransactionModalController = function ($scope, $modalInstance, budget, TransactionService) {

  $scope.selected = budget;
  $scope.original = angular.copy(budget);
  $scope.transaction = {budget: {id: budget.id}};

  $scope.errorMessage = errorMessage;
  $scope.errorClass = errorClass;

  // TODO https://github.com/angular-ui/bootstrap/issues/969
  $scope.ok = function (form) {
    $scope.transaction.loading = true;
    TransactionService.save($scope.transaction).$promise.then(function() {
      budget.actual += $scope.transaction.amount;
      $scope.transaction.loading = false;
      $modalInstance.close();
    }, function(response) {
      $scope.form = form;
      $scope.transaction.loading = false;
      failure($scope, response);
    });

  };

  $scope.cancel = function () {
    $scope.selected = $scope.original;
    $modalInstance.dismiss('cancel');
  };
};

var TransactionsModalController = function ($scope, $modalInstance, budget, transactions) {

  $scope.selected = budget;
  $scope.transactions = transactions;
  $scope.cancel = function () {
    $modalInstance.dismiss('cancel');
  };
};

financeControllers.controller('BudgetController', function ($scope, CategoryService, BudgetService) {

  // cache categories, reuse when validation failed.
  var categories = null;
  $scope.loaded = false;
  $scope.budget = {};

  $scope.errorMessage = errorMessage;
  $scope.errorClass = errorClass;

  CategoryService.query(function(response) {
    categories = response;
    $scope.categories = response;
    $scope.loaded = true;
  });

  $scope.createBudget = function() {
    $scope.success = false;
    $scope.error = false;
    $scope.message = null;
    var budget = $scope.budget;
    if(budget.category) {
      budget.categoryId = budget.category.id;
    } else {
      budget.categoryId = null;
    }
    BudgetService.save($scope.budget).$promise.then(function() {
      $scope.success = true;
      $scope.message = "Successfully created Budget";
      $scope.budget = {};
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

financeControllers.controller('RecurringsController', function ($scope, $modal, appConfig, RecurringService) {

  $scope.loaded = false;
  $scope.recurrings = RecurringService.query(function() {
    $scope.loaded = true;
  });

  $scope.confirmDelete = function (recurring) {
    var modalInstance = $modal.open({
      templateUrl: '/app/partials/confirmDeleteModal.html?' + appConfig.version,
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
    }, function (response) {
      errorModal($modal, response.data.errors);
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

financeControllers.controller('RecurringController', function ($scope, RecurringService, BudgetService) {


  // cache budgets, reuse when validation failed.
  var budgets = null;
  $scope.loaded = false;
  $scope.recurring = {};

  $scope.errorMessage = errorMessage;
  $scope.errorClass = errorClass;

  BudgetService.query(function(response) {
    budgets = response;
    $scope.budgets = response;
    $scope.loaded = true;
  });

  $scope.createRecurring = function() {
    $scope.success = false;
    $scope.error = false;
    $scope.message = null;
    var recurring = $scope.recurring;
    if(recurring.budget) {
      recurring.budgetId = recurring.budget.id;
    } else {
      recurring.budgetId = null;
    }
    RecurringService.save($scope.recurring).$promise.then(function() {
      $scope.success = true;
      $scope.message = "Successfully created Recurring";
      $scope.recurring = {};
      clearErrors($scope);
      // repopulate budgets
      $scope.budgets = budgets;
    }, function(response) {
      failure($scope, response)
      // repopulate budgets
      $scope.budgets = budgets;
    });
  }
});


financeControllers.controller('CategoriesController', function ($scope, $modal, appConfig, CategoryService) {

  $scope.loaded = false;
  $scope.categories = CategoryService.query(function() {
    $scope.loaded = true;
  });

  $scope.confirmDelete = function (category) {
    var modalInstance = $modal.open({
      templateUrl: '/app/partials/confirmDeleteModal.html?' + appConfig.version,
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

  $scope.openBudgetsModal = function (category) {
    $scope.budgetsLoaded = false;

    $modal.open({
      templateUrl: '/app/partials/budgets.html',
      scope: $scope,
      controller: BudgetsModalController,
      size: 'lg',
      resolve: {
        category: function() {
          return category;
        },
        budgets: function() {
          return CategoryService.budgets(category, function() {
            $scope.budgetsLoaded = true;
          });
        }
      }
    });
  };
});

financeControllers.controller('BudgetsController', function ($scope, $modal, appConfig, BudgetService) {

  $scope.loaded = false;
  $scope.budgets = BudgetService.query(function() {
    $scope.loaded = true;
  });

  $scope.confirmDelete = function (budget) {
    var modalInstance = $modal.open({
      templateUrl: '/app/partials/confirmDeleteModal.html?' + appConfig.version,
      controller: ConfirmDeleteModalController,
      resolve: {
        modal: function() {
          return budget;
        },
        message: function() {
          return ' this budget';
        }
      }
    });

    modalInstance.result.then(function (selected) {
      selected.$delete({id: selected.id}, function() {
        $scope.budgets.splice($scope.budgets.indexOf(selected), 1);
      }, function(response) {
        errorModal($modal, response.data.errors);
      });
    });
  };
});

var BudgetsModalController = function ($scope, $modalInstance, category, budgets) {
  $scope.selected = category;
  $scope.budgets = budgets;
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
  });
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
  $location.url("/");

  $scope.logout = function() {
    auth.clearCredentials();
    AuthenticationService.isLogged = false;
    delete $window.sessionStorage.token;
    delete $rootScope.nav;
    delete $rootScope.user;
    delete $rootScope.username;
    $location.url("/");
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
    if (field && field.$setValidity) {
      field.$dirty = false;
      field.$setValidity("server", true);
    }
  });
};

function clearErrors($scope) {
  $scope.response = {};
  _.each($scope.form, function (field) {
    if(field && field.$setValidity) {
      field.$setValidity("server", true);
    }
  });
};

function failure($scope, response) {

  $scope.response = response.data;

  _.each($scope.form, function (field) {
    if(field && field.$setValidity) {
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
