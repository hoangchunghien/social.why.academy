var app = angular.module('myApp', [
    'ngCookies',
    'creator.users.service'
]);

app.config(function($httpProvider) {
    //Enable cross domain calls
    $httpProvider.defaults.useXDomain = true;

    //Remove the header used to identify ajax call  that would prevent CORS from working
    delete $httpProvider.defaults.headers.common['X-Requested-With'];
});

console.log("Loading path service...");
app.service('path', function(api) {
    this.getPathVariable = function(regrexPt) {
        var reg = RegExp(regrexPt);
        var result = reg.exec(window.location.pathname);
        // console.log(result);
        return result;
    }
});

console.log("Loading upload service...");
app.service('uploadSrv', function() {
    this.upload = function(file, name, onProgressCallback, onSuccessCallback) {
        var fd = new FormData();
        fd.append('file', file);
        fd.append('name', name);
        // console.log(fd);
        var xhr = new XMLHttpRequest();
        xhr.open('POST', API_SERVER_URL + '/photos', true);
        xhr.upload.onprogress = function(e) {
            onProgressCallback(e);
        };
        xhr.onload = function() {
            if (this.status == 200) {
                var resp = JSON.parse(this.response);
                onSuccessCallback(resp);
            }
        };
        xhr.send(fd);
    }
});

console.log("Loading api factory...");
app.factory('apiUrl', function() {
    return {
        apiServerUrl : function() {
            if (window.location.hostname === "localhost") {
                return "http://localhost:8080";
            }
            else {
                return "http://api.why.academy";
            }
        }
    };
});
//
//console.log("Loading jquery datatable directive...");
//// Pass a table element id to load jquery datatable
//app.directive('jqueryDatatable',function( $timeout){
//    return{
//        restrict:'A',
//
//        link:function(scope,elem,attrs){
//
//            if(scope.$last){
//                $timeout(function(){
//                    $(document).ready(function() {
//                        attrs.$observe('jqueryDatatable', function (value) {
//                            if (value) {
//                                var tbElem = $('#'+value);
//                                var unSortCols = attrs.unSortCols || "";
//                                console.log(unSortCols);
//                                var dtTableProperties = {
//                                    "iDisplayLength": 100
//                                };
//                                if (unSortCols !== "") {
//                                    dtTableProperties["aoColumnDefs"] = [
//                                        { 'bSortable': false, 'aTargets': unSortCols.split(',').map( Number ) }
//                                    ]
//                                }
//                                var dtTable = tbElem.dataTable(dtTableProperties);
//                                var sCol = attrs.sortedCol || 0;
//                                var sType = attrs.sortedType || "asc";
//                                dtTable.fnSort( [ [sCol,sType] ] );
//                            }
//                        });
//                    } );
//                },0)
//            }
//        }
//    }
//});

var API_SERVER_URL;

var loadEnv = function() {
    if (window.location.hostname === "localhost") {
        API_SERVER_URL = "http://localhost:8080";
    }
    else {
        API_SERVER_URL = "http://api.why.academy";
    }
};
loadEnv();
