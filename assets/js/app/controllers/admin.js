
app.controller('LessonCtrl', function($scope, $http, userSrv) {

    console.log("user " + userSrv.getProfile());
    $scope.status = [
        {'text': 'Prepare for creating', 'value': 1},
        {'text': 'Waiting for creating', 'value': 2},
        {'text': 'Creation received', 'value': 3},
        {'text': 'Waiting for review', 'value': 4},
        {'text': 'In review', 'value': 5},
        {'text': 'Pending contract', 'value': 6},
        {'text': 'Pending creator release', 'value': 7},
        {'text': 'Pending store release', 'value': 8},
        {'text': 'Processing for store', 'value': 9},
        {'text': 'Ready for sale', 'value': 10},
        {'text': 'Rejected', 'value': 11},
        {'text': 'Removed from sale', 'value': 12}
    ];

    $scope.id;
    $scope.course;

    $scope.changedLessons = [];
    $scope.lessonStatus = {};

    $scope.changeStatus = function(lesson) {
        var found = false;
        var index;
        for (var i = 0; i < $scope.changedLessons.length; i++) {
            if ($scope.changedLessons[i].id === lesson.id) {
                found = true;
                index = i;
                break;
            }
        }
        if (found) {
            $scope.changedLessons[index].status = $scope.lessonStatus[lesson.name].value;
            console.log(JSON.stringify($scope.changedLessons));
        }
        else {
            $scope.changedLessons.push({'id': lesson.id, 'status': $scope.lessonStatus[lesson.name].value});
            console.log(JSON.stringify($scope.changedLessons));
        }
    };

    $scope.saveChanged = function() {
        if (userSrv.isAuthenticated()) {
            var accessToken = userSrv.getToken().value;
            $http({
                method: 'PUT',
                url: API_SERVER_URL + "/lessons",
                params: {'lessons': JSON.stringify($scope.changedLessons)},
                headers: {'Content-Type': 'application/x-www-form-urlencoded', 'Access-Token': accessToken}

            }).success(function(data, status) {
                $scope.init($scope.id);
            });
        }
        console.log("Require authenticating to perform this function");
    };

    $scope.init = function(cid) {
        $scope.id = cid;
        $scope.changedLessons = [];
        $scope.lessonStatus = {};
        $http({
            method: 'GET',
            url: API_SERVER_URL + "/v2/courses/" + cid,
            params: {
                'include': "lessons",
                'q[lessons.status]': "0,1,2,3,4,5,6,7,8,9,11,12,13",
                'fields[lessons]': 'id,name,picture_url,type,language,audio_url,content,status,created_at',
                'sort[lessons]': "created_at"
            },
            headers: {'Content-Type': 'application/x-www-form-urlencoded'}

        }).success(function(data, status) {

            $scope.course = data.courses[0];
            $scope.loadLessonsStatus();
        }).error(function(data, status) {
            $scope.status = status;
            $scope.data = data || "Request failed";
            //console.log("failed");
        });
    };

    $scope.loadLessonsStatus = function() {
        var lessons = $scope.course.lessons;
        for (var i = 0; i < lessons.length; i++) {
            $scope.lessonStatus[lessons[i].name] = $scope.status[lessons[i].status - 1];
        }
    };

    $scope.playAudio = function(audioUrl) {
        //playAudio(audioUrl);
        var mySound = soundManager.createSound({
            url: audioUrl
        });
        mySound.play();
    };

    /*
     * Ultilities --------------------------------------------------------------
     */
    $scope.refreshSoundUrl = function() {
        $scope.lessonSoundUrl = "https://ssl.gstatic.com/dictionary/static/sounds/de/0/" + $scope.lessonName + ".mp3";
        $('#txtLessonSoundUrl').val($scope.lessonSoundUrl);
    };

    $scope.getStatusIcon = function(status) {
        var icon;
        switch (status) {
            case 5:
                icon = "glyphicon glyphicon-exclamation-sign";
                break;
            case 10:
                icon = "glyphicon glyphicon-ok-sign";
                break;
            default:
                icon = "glyphicon glyphicon-question-sign";
                break;
        }
        return icon;
    };

    $scope.getStatusColor = function(status) {
        var color;
        switch (status) {
            case 5:
                color = "orange";
                break;
            case 10:
                color = "green";
                break;
            default:
                color = "yellow";
                break;
        }
        return color;
    };

}
);

