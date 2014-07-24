
app.controller('HomeCtrl', function ($scope, $http, user) {

    $scope.init = function() {

        if (user.isAuthenticated()) {
//            var profile = user.getProfile();
//            console.log(profile);
//            window.location.href = "/users/" + profile.id + "/courses";
        }
        else {

            // window.location.href = "/courses";
        }
    };


});