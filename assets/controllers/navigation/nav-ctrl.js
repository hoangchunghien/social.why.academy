/**
 * Created by Hien on 5/30/2014.
 */

angular.module('arena.navigation.controller', [
    'ui.router',
    'arena.users.service'
])
    .controller('NavCtrl', function ($scope, $http, userSrv) {

        $scope.user = null;

        $scope.$on('$stateChangeStart', function(event, toState, toParams, fromState, fromParams) {
            $scope.loading = true;
        });

        $scope.$on('$stateChangeSuccess', function(event, toState, toParams, fromState, fromParams) {
            $scope.loading = false;
        });

        $scope.init = function () {
            document.cookie = decodeURIComponent(document.cookie);
            $scope.profile = userSrv.getProfile() || {};
            $scope.authenticated = userSrv.isAuthenticated();
            $scope.loadUserNavBar();
        };

        $scope.doLogin = function() {
            window.location.href = "/login/facebook";
        };

        $scope.doLogout = function () {
            // user.logout();
            window.location.href = "/logout";
            $scope.init();
        };

        $scope.loadUserNavBar = function () {
            $scope.isAuth = false;
            if (userSrv.isAuthenticated()) {
                $scope.isAuth = true;
            }
            else {
                $scope.isAuth = false;
            }

            if (window.location.pathname === "/users/" + $scope.profile.id + "/courses") {
                $("#myCoursesNav").addClass("active");
            }
            else if (window.location.pathname === "/courses") {
                $("#publicCoursesNav").addClass("active");
            }
        };

        $scope.getLoginOrLogoutUrl = function () {
            var url = "";
            if ($scope.user.authenticated) {
                url = "/logout";
            }
            else {
                url = "/login/facebook";
            }
            return url;
        };


    });