
app.controller('LessonCtrl', function($scope, $http, user) {
    $scope.languages = [{'name': 'English', 'value': 'en'}];
    $scope.types = [{'name': 'Word', 'value': 'word'}];

    $scope.course;

    $scope.lessonPictureUrl;
    $scope.lessonSoundUrl;
    $scope.lessonName;
    $scope.lessonType = $scope.types[0];
    $scope.lessonContent = {};
    $scope.lessonContent['examples'] = [{'text': ''}];
    $scope.lessonContent['phonetics'] = [{'text': ''}];
    $scope.lessonLanguage = $scope.languages[0];
    $scope.lessonDifficultyLevel = 0;
    $scope.lessonId;

    $scope.resetEditor = function() {
        $scope.lessonPictureUrl = null;
        $scope.lessonSoundUrl = null;
        $scope.lessonName = null;
        $scope.lessonType = $scope.types[0];
        $scope.lessonContent = {};
        $scope.lessonContent['examples'] = [{'text': ''}];
        $scope.lessonContent['phonetics'] = [{'text': ''}];
        $scope.lessonLanguage = $scope.languages[0];
        $scope.lessonDifficultyLevel = 0;
        $scope.lessonId = null;
    };

    $scope.init = function(cid) {
        console.log("Initializing data...");
        $http({
            method: 'GET',
            url: API_SERVER_URL + "/v2/courses/" + cid,
            params: {
                'include': "lessons",
                'fields[lessons]': "id,name,picture_url,difficulty_level,type,language,audio_url,content,status,created_at",
                'q[lessons.status]': "0,1,2,3,4,5,6,7,8,9,10,11,12,13",
                'sort[lessons]': "-created_at"
            },
            headers: {'Content-Type': 'application/x-www-form-urlencoded; charset=utf-8'}

        }).success(function(data, status) {
            console.log("Data initialized");
            $scope.course = data.courses[0];
            initPhonetics();

        }).error(function(data, status) {
            $scope.status = status;
            $scope.data = data || "Request failed";
            //console.log("failed");
        });
    };

    var initPhonetics = function() {
        console.log('Initializing phonetics...');
        for (var i = 0; i < $scope.course.lessons.length; i++) {
            initPhonetic($scope.course.lessons[i]);
        }
    };

    var initPhonetic = function(lesson) {
        var content = lesson.content;
        if (content) {
            content = JSON.parse(content);
            lesson.phonetics = content.phonetics;
        }
    };

    /*
     * Validator ---------------------------------------------------------------
     */
    var validateData = function() {
        var valid = true;

        var validName = $scope.validateLessonName();
        var validType = $scope.validateLessonType();
        var validLevel = $scope.validateDifficultyLevel();
        //var validAudio = $scope.validateLessonAudioUrl();
        //var validPicture = $scope.validateLessonPictureUrl();

        valid = (validName && validType && validLevel);
        return valid;
    };

    $scope.validateLessonName = function() {
        var valid = true;
        if (!$scope.lessonName) {
            $("#frmLessonName").addClass("has-error");
            valid = false;
        }
        else {
            $("#frmLessonName").removeClass("has-error");
        }
        return valid;
    };

    $scope.validateLessonType = function() {
        var valid = true;
        if (!$scope.lessonType) {
            $('#frmLessonType').addClass("has-error");
            valid = false;
        }
        else {
            $('#frmLessonType').removeClass("has-error");
        }
        return valid;
    };

    $scope.validateDifficultyLevel = function() {
        var valid = true;
        if (typeof $scope.lessonDifficultyLevel !== 'number') {
            $("#frmDifficultyLevel").addClass("has-error");
            valid = false;
        }
        else {
            $("#frmDifficultyLevel").removeClass("has-error");
        }
        return valid;
    };

    $scope.validateLessonAudioUrl = function() {
        var valid = true;
        if (!$scope.lessonSoundUrl) {
            valid = false;
        }
        else {
            valid = true;
        }

        if (valid) {
            $("#frmLessonSoundUrl").removeClass("has-error");
            return true;
        }
        else {
            $("#frmLessonSoundUrl").addClass("has-error");
            return false;
        }
    };

    $scope.enablePlaySoundInEditor = function() {
        $('#iconPlaySoundForEditor').addClass('glyphicon-cloud-download');

        var checkSound = $scope.checkSoundUrl(function(enabled) {
            $('#iconPlaySoundForEditor').removeClass('glyphicon-cloud-download');
            if (enabled) {
                $('#btnPlaySoundForEditor').prop('disabled', false);
                $('#iconPlaySoundForEditor').removeClass('glyphicon-volume-off');
                $('#iconPlaySoundForEditor').addClass('glyphicon-volume-up');
                $("#frmLessonSoundUrl").removeClass("has-error");
            }
            else {
                $('#btnPlaySoundForEditor').prop('disabled', true);
                $('#iconPlaySoundForEditor').removeClass('glyphicon-volume-up');
                $('#iconPlaySoundForEditor').addClass('glyphicon-volume-off');
            }
        });

        checkSound;
    };

    $scope.checkSoundUrl = function(callback) {
        var audio = new Audio();
        var timer;
        audio.oncanplay = function() {
            console.log("Sound url valid");
            clearTimeout(timer);
            callback(true);
        };
        audio.onerror = function() {
            console.log("Sound url unvalid");
            clearTimeout(timer);
            callback(false);
        };
        audio.src = $scope.lessonSoundUrl;
        timer = setTimeout(function() {
            console.log("Load sound by url timeout, reload");
            audio.src = $scope.lessonSoundUrl;
            timer;
        }, 3000);
    };

    $scope.validateLessonPictureUrl = function() {
        var valid = true;
        if (!$scope.lessonPictureUrl) {
            $("#frmLessonPictureUrl").addClass("has-error");
            valid = false;
        }
        else {
            $("#frmLessonPictureUrl").removeClass("has-error");
        }
        return valid;
    };

    $scope.enableShowPictureForEditor = function() {
        $('#iconShowPictureForEditor').addClass('glyphicon-cloud-download');
        $('#btnShowPictureForEditor').popover('hide');
        $scope.checkPictureUrl(function(enable) {
            $('#iconShowPictureForEditor').removeClass('glyphicon-cloud-download');
            if (enable) {
                $('#btnShowPictureForEditor').prop('disabled', false);
                $('#iconShowPictureForEditor').removeClass('glyphicon-eye-close');
                $('#iconShowPictureForEditor').addClass('glyphicon-eye-open');
            }
            else {
                $('#btnShowPictureForEditor').prop('disabled', true);
                $('#iconShowPictureForEditor').removeClass('glyphicon-eye-open');
                $('#iconShowPictureForEditor').addClass('glyphicon-eye-close');
            }
        });
    };

    $scope.checkPictureUrl = function(callback) {
        var img = new Image();
        var timer;
        img.onload = function() {
            console.log("Picture url valid");
            clearTimeout(timer);
            callback(true);
        };
        img.onerror = function() {
            clearTimeout(timer);
            callback(false);
        };
        img.src = $scope.lessonPictureUrl;
        timer = setTimeout(function() {
            console.log("Load picture by url timeout, reload");
            img.src = $scope.lessonPictureUrl;
            timer;
        }, 3000);
    };

    var typingTimer;
    var doneTypingInterval = 900;
    $scope.handleTypingName = function() {
        clearTimeout(typingTimer);
        typingTimer = setTimeout(function() {
            $scope.refreshSoundUrl();
            $scope.retrievePhonetic($scope.lessonName);
        }, doneTypingInterval);
    };

    /*
     * Ultilities --------------------------------------------------------------
     */
    $scope.retrievePhonetic = function(word) {
        var url = "http://tudienhinh.com/api/" + word;
        $http({
            method: 'GET',
            url: url

        }).success(function(data, status) {
            if (data['phonetic']) {
                $scope.lessonContent['phonetics'] = [{'text': data['phonetic']}, {'text': ''}];
            }
        }).error(function(data, status) {
            $scope.status = status;
            $scope.data = data || "Request failed";
            //console.log("failed");
        });
    };

    $scope.refreshSoundUrl = function() {
        if (!$scope.lessonId) {
            if ($scope.lessonName) {
                $scope.lessonSoundUrl = "https://ssl.gstatic.com/dictionary/static/sounds/de/0/" + $scope.lessonName + ".mp3";
            }
            else {
                $scope.lessonSoundUrl = "";
            }
            $('#txtLessonSoundUrl').val($scope.lessonSoundUrl);
        }
    };
//
//    $scope.getStatusIcon = function(status) {
//        var icon;
//        switch (status) {
//            case 5:
//                icon = "glyphicon glyphicon-exclamation-sign";
//                break;
//            case 10:
//                icon = "glyphicon glyphicon-ok-sign";
//                break;
//            default:
//                icon = "glyphicon glyphicon-question-sign";
//                break;
//        }
//        return icon;
//    };
//
//    $scope.getStatusColor = function(status) {
//        var color;
//        switch (status) {
//            case 5:
//                color = "orange";
//                break;
//            case 10:
//                color = "green";
//                break;
//            default:
//                color = "yellow";
//                break;
//        }
//        return color;
//    };

    $('#btnShowPictureForEditor').popover({
        html: true,
        trigger: 'click',
        animation: true,
        title: 'Picture',
        placement: 'top',
        content: function() {
            return '<img src="' + $scope.lessonPictureUrl + '" height="100">';
        }
    });
});
