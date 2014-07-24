/**
 * Created by Hien on 5/30/2014.
 */

angular.module('arena.users.service', [
    'ngCookies'
])
    .service('userSrv', function ($cookies) {

        var authData = null;

        var getUserFromCookies = function () {
            document.cookie = decodeURIComponent(document.cookie);
            if (authData === null) {
                var user = null;
                if ($cookies.user) {
                    user = $cookies.user;
                    user = JSON.parse(user.replace("j:", ""));
                }
                authData = user;
            }
            return authData;
        };

        this.isAuthenticated = function () {
            var authenticated = false;
            var user = getUserFromCookies();
            if (user) {
                var expires = new Date(user.token.expires);
                var now = new Date();
                if (now < expires) {
                    authenticated = true;
                }
            }
            return authenticated;
        }

        this.logout = function () {
            delete $cookies.user;
            authData = null;
        }

        this.getProfile = function () {
            if (this.isAuthenticated()) {
                var user = getUserFromCookies();
                var profile = user.profile;
                return profile;
            }
            return null;
        }

        this.getToken = function () {
            var user = getUserFromCookies();
            if (this.isAuthenticated()) {
                var token = user.token;
                return token;
            }
            return null;
        }

    });