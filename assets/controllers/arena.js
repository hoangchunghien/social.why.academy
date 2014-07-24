/**
 * Created by Hien on 5/30/2014.
 */

angular.module('arena', [
    'arena.main',
    'arena.api.service',
    'arena.utils.service',
    'arena.navigation.controller',
    'ui.router'
]).factory('Seo', function () {
    return {
        title: 'The arena for languages'
    };
})
    .run(
    ['Seo', '$rootScope', '$state', '$stateParams',
        function (Seo, $rootScope, $state, $stateParams) {
            $rootScope.Seo = Seo;
            $rootScope.$state = $state;
            $rootScope.$stateParams = $stateParams;
        }
    ]
)
    .config([
        '$stateProvider', '$urlRouterProvider', '$locationProvider',
        function ($stateProvider, $urlRouterProvider, $locationProvider) {
            $locationProvider.html5Mode(true);
        }
    ]
);