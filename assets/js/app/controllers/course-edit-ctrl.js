app.controller('CourseEditCtrl', function ($scope, $http, user, apiUrl, path, uploadSrv, courseSrv) {

    var apiServerUrl = apiUrl.apiServerUrl();
    var regPt = "/courses/([0-9]*)(/?[a-z|_]*)";
    var pathtId = path.getPathVariable(regPt)[1] || null;
    var action = path.getPathVariable(regPt)[2];
    console.log("action: " + action);
    $scope.contentTypes = [
        {name:"course", value: 'course'},
        {name:'lesson', value:'lesson'}
    ];
    $scope.courseType = {};

    $scope.parent;
    $scope.course;
    $scope.courseType = $scope.contentTypes[1];

    $scope.previousValues = {}; // For tracking change to update

    $scope.init = function () {
        loadCourse(action);
        loadTitle();
    };


    $scope.save = function() {
        if (action === "new" || action === "/add_course") {
            doCreate();
        }
        else if (action === "/edit") {
            doUpdate();
        }
    };

    $scope.checkChanged = function() {
        checkForAllowSave();
    };

    var checkForAllowSave = function() {

        if (hasChanged()) {
            $("#btnSave").removeAttr('disabled');
        }
        else {
            $("#btnSave").attr('disabled', 'disabled');
        }
    };

    var doCreate = function() {
        var course = {};
        course.name = $scope.course.name;
        course.content_type = $scope.courseType.value;
        course.description = $scope.course.description;
        course.picture_url = $scope.course.picture_url;
        if ($scope.parent) {
            course.parent = {id: $scope.parent.id};
        }
        console.log(JSON.stringify(course));
        courseSrv.create(course,
            function (progress) {

            },
            function (response) {
                console.log(response);
                window.location.href = "/courses/" + response.course.id;
            }
        );
    };

    var hasChanged = function() {
        if ($scope.previousValues.name !== $scope.course.name ||
            $scope.previousValues.picture_url !== $scope.course.picture_url ||
            $scope.previousValues.description !== $scope.course.description) {
            return true;
        }
        return false;
    }


    var loadUIControl = function() {
        $scope.checkChanged();
    };

    var loadChangeTracking = function() {
        $scope.previousValues['name'] = $scope.course.name;
        $scope.previousValues['picture_url'] = $scope.course.picture_url;
        $scope.previousValues['description'] = $scope.course.description;
    };

    var loadTitle = function () {
        $scope.editorTitle = getTitle(action);
        console.log($scope.editorTitle);
    };

    var loadCourse = function(action) {
        if (action === "/edit") {
            getCourse(pathtId, function(course) {
                $scope.course = course;
                $scope.courseType = getContentType(course.content_type);
                $("#slCourseType").attr("disabled", "disabled");
                loadParent(action);
                loadChangeTracking();
                loadUIControl();
            });
        }
        else if (action === "/add_course") {
            loadParent(action);
        }
    };

    var loadParent = function(action) {
        if (action === "/add_course") {
            getCourse(pathtId, function(course) {
                $scope.hasParent = true;
                $scope.parent = course;
                console.log(course);
            });
        }
        else if (action == "/edit" && $scope.course.parent) {
            getCourse($scope.course.parent.id, function(course) {
                $scope.hasParent = true;
                $scope.parent = course;
            });
        }
    };

    var getCourse = function(id, callback) {
        $http({
            method: 'GET',
            url: apiServerUrl + "/v2/courses/" + id,
            params: {
                'include': "parent"
            },
            headers: {'Content-Type': 'application/x-www-form-urlencoded; charset=utf-8'}

        }).success(function(data, status) {
            console.log(data.courses);
            if (data.courses.length > 0) {
                callback(data.courses[0]);
            }
        }).error(function(data, status) {
            $scope.status = status;
            $scope.data = data || "Request failed";
            console.log("failed");
        });
    };

    var getTitle = function (action) {
        switch (action) {
            case "/edit":
                return "Edit course";
            case "/add_course":
                return "Add subcourse";
            default:
                return "Create new course";
        }
    };
});