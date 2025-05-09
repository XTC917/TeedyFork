'use strict';

/**
 * User request service.
 */
angular.module('docs').factory('UserRequest', function(Restangular, $http) {
  return {
    /**
     * Creates a new user request.
     */
    create: function(userRequest) {
      return Restangular.one('userrequest').put(userRequest);
    },
    
    /**
     * Gets all pending user requests.
     */
    list: function() {
      return Restangular.one('userrequest').get();
    },
    
    /**
     * Processes a user request.
     */
    process: function(id, action) {
      return $http({
        method: 'POST',
        url: '/docs-web/api/userrequest/' + id,
        data: $.param({action: action}),
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded'
        }
      });
    }
  }
}); 