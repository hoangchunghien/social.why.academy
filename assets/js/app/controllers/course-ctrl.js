
app.controller('CourseCtrl', function ($scope, $http, user, apiUrl) {
    var apiServerUrl = apiUrl.apiServerUrl();

    $scope.courses = [];

    $scope.init = function() {
        $http({
            method: 'GET',
            url: apiServerUrl + "/v2/courses",
            params: {
                'q[status]': '10'
            },
            headers: {'Content-Type': 'application/x-www-form-urlencoded; charset=utf-8'}

        }).success(function(data, status) {

            $scope.courses = data.courses;
            console.log($scope.courses);

        }).error(function(data, status) {
            $scope.status = status;
            $scope.data = data || "Request failed";
            //console.log("failed");
        });
    }

});