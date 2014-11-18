
var financeServices = angular.module('financeServices', ['ngResource', 'ngCookies']);

financeServices.factory('CategoryService', function($resource) {
  return $resource("/api/categories/:id/:path", {}, {
    summary: {method: 'GET', params: {path: 'summary'}, isArray: true},
    budgets: {method: 'GET', params: {id: '@id', path: 'budgets'}, isArray: true}
  });
});

financeServices.factory('BudgetService', function($resource) {
  return $resource("/api/budgets/:id/:transactions", {}, {
    update: {method: 'PUT', params: {id: '@id'}}
  });
});

financeServices.factory('TransactionService', function($resource) {
  return $resource("/api/transactions/:id/:path", {}, {
    summary: {method: 'GET', params: {path: 'summary'}, isArray: true},
    monthly: {method: 'GET', params: {path: 'monthly'}, isArray: true},
    todayRecurrings: {method: 'GET', params: {path: 'today'}, isArray: true}
  });
});

financeServices.factory('RecurringService', function($resource) {
  return $resource("/api/recurrings/:id/:path", {}, {
    transactions: {method: 'GET', params: {path: 'transactions'}, isArray: true}
  });
});

financeServices.factory('UserService', function($resource) {
  return $resource("/api/users/:path", {}, {
    login: {method: 'POST', params: {path: 'auth'}},
    signup: {method: 'POST'},
    update: {method: 'PUT'},
    changePassword: {method: 'PUT',  params: {path: 'password'}},
    account: {method: 'GET', params: {path: 'account'}},
    usage: {method: 'GET', params: {path: 'usage'}},
    ping: {method: 'GET', params: {path: 'ping'}}
  });
});

financeServices.factory('ReportService', function($resource) {
  return $resource("/api/reports/:path", {}, {
    transactions: {method: "POST", isArray: true, params: {path: 'transactions'}}
  });
});

financeServices.factory('AuthenticationService', function() {
  var auth = {
    user: null,
    isLogged: false,
    anonymous: ['/', '/signup']
  };
  return auth;
});

financeServices.factory('TokenInterceptor', function ($rootScope, $q, $window, $location, AuthenticationService) {
  return {
    request: function (config) {
      config.headers = config.headers || {};
      if ($window.sessionStorage.token) {
        config.headers.Authorization = 'Bearer ' + $window.sessionStorage.token;
      }
      return config;
    },

    requestError: function(rejection) {
      return $q.reject(rejection);
    },

    /* Set Authentication.isAuthenticated to true if 200 received */
    response: function (response) {
      if (response != null && response.status == 200 && $window.sessionStorage.token && !AuthenticationService.isAuthenticated) {
        AuthenticationService.isAuthenticated = true;
      }
      return response || $q.when(response);
    },

    /* Revoke client authentication if 401 is received */
    responseError: function(rejection) {
      if (rejection != null && rejection.status === 401) {
        delete $window.sessionStorage.token;
        delete $rootScope.user;
        AuthenticationService.isAuthenticated = false;
        $location.url("/");
      }

      return $q.reject(rejection);
    }
  };
});

financeServices.factory('auth', ['Base64', '$cookieStore', '$http', function (Base64, $cookieStore, $http) {
  // initialize to whatever is in the cookie, if anything
  $http.defaults.headers.common['Authorization'] = 'Basic ' + $cookieStore.get('auth');

  return {
    setCredentials: function (username, password) {
      var encoded = Base64.encode(username + ':' + password);
      $http.defaults.headers.common['Authorization'] = 'Basic ' + encoded;
      $cookieStore.put('auth', encoded);
    },
    clearCredentials: function () {
      document.execCommand("ClearAuthenticationCache");
      $cookieStore.remove('auth');
      $http.defaults.headers.common.Authorization = 'Basic ';
    }
  };
}]);

financeServices.factory('Base64', function() {
  var keyStr = 'ABCDEFGHIJKLMNOP' +
      'QRSTUVWXYZabcdef' +
      'ghijklmnopqrstuv' +
      'wxyz0123456789+/' +
      '=';
  return {
    encode: function (input) {
      var output = "";
      var chr1, chr2, chr3 = "";
      var enc1, enc2, enc3, enc4 = "";
      var i = 0;

      do {
        chr1 = input.charCodeAt(i++);
        chr2 = input.charCodeAt(i++);
        chr3 = input.charCodeAt(i++);

        enc1 = chr1 >> 2;
        enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
        enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
        enc4 = chr3 & 63;

        if (isNaN(chr2)) {
          enc3 = enc4 = 64;
        } else if (isNaN(chr3)) {
          enc4 = 64;
        }

        output = output +
            keyStr.charAt(enc1) +
            keyStr.charAt(enc2) +
            keyStr.charAt(enc3) +
            keyStr.charAt(enc4);
        chr1 = chr2 = chr3 = "";
        enc1 = enc2 = enc3 = enc4 = "";
      } while (i < input.length);

      return output;
    },

    decode: function (input) {
      var output = "";
      var chr1, chr2, chr3 = "";
      var enc1, enc2, enc3, enc4 = "";
      var i = 0;

      // remove all characters that are not A-Z, a-z, 0-9, +, /, or =
      var base64test = /[^A-Za-z0-9\+\/\=]/g;
      if (base64test.exec(input)) {
        alert("There were invalid base64 characters in the input text.\n" +
            "Valid base64 characters are A-Z, a-z, 0-9, '+', '/',and '='\n" +
            "Expect errors in decoding.");
      }
      input = input.replace(/[^A-Za-z0-9\+\/\=]/g, "");

      do {
        enc1 = keyStr.indexOf(input.charAt(i++));
        enc2 = keyStr.indexOf(input.charAt(i++));
        enc3 = keyStr.indexOf(input.charAt(i++));
        enc4 = keyStr.indexOf(input.charAt(i++));

        chr1 = (enc1 << 2) | (enc2 >> 4);
        chr2 = ((enc2 & 15) << 4) | (enc3 >> 2);
        chr3 = ((enc3 & 3) << 6) | enc4;

        output = output + String.fromCharCode(chr1);

        if (enc3 != 64) {
          output = output + String.fromCharCode(chr2);
        }
        if (enc4 != 64) {
          output = output + String.fromCharCode(chr3);
        }

        chr1 = chr2 = chr3 = "";
        enc1 = enc2 = enc3 = enc4 = "";

      } while (i < input.length);

      return output;
    }
  };
});
