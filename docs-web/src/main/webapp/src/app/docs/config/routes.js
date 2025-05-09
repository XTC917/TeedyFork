'use strict';

/**
 * Routes configuration.
 */
angular.module('docs').config(function($stateProvider, $stateParams) {
  $stateProvider
    .state('userrequest', {
      url: '/userrequest',
      templateUrl: 'app/docs/view/userrequest.html',
      controller: 'UserRequest'
    })
    .state('usergroup', {
      url: '/usergroup',
      templateUrl: 'src/app/docs/view/usergroup/UserList.html',
      controller: 'UserList'
    })
    .state('user', {
      url: '/user/:username',
      templateUrl: 'src/app/docs/view/usergroup/UserProfile.html',
      controller: 'UserProfile'
    })
    .state('chat', {
      url: '/chat/:userId',
      templateUrl: 'app/docs/view/chat/chat.html',
      controller: 'ChatController',
      resolve: {
        userId: function($stateParams) {
          return $stateParams.userId;
        }
      }
    });
}); 