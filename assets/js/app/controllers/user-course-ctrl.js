

app.controller('UserCourseCtrl', function ($scope, $http, user, apiUrl, path) {

    var apiServerUrl = apiUrl.apiServerUrl();
    var regPt = "/users/([0-9]*)/?\s*";
    var uid = path.getPathVariable(regPt)[1];
    console.log("uid: " + uid);
    $scope.courses = [];

    $scope.init = function() {
        $http({
            method: 'GET',
            url: apiServerUrl + "/v2/courses",
            params: {
                'q[user.id]': uid,
                'fields[]': 'id,name,picture_url,content_type,description,status'
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