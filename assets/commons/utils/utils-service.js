/**
 * Created by Hien on 5/31/2014.
 */

angular.module('arena.utils.service', [

])
    .factory('utils', function () {
        return {
            // Util for finding an object by its 'id' property among an array
            findById: function (a, id) {
                console.log(JSON.stringify(a));
                for (var i = 0; i < a.length; i++) {
                    console.log(a[i]);
                    if (a[i].id == id) return a[i];
                }
                return null;
            },

            remove: function(array, id) {
                for (var i = 0; i < array.length; i++) {
                    if (array[i].id == id) {
                        array.splice(i, 1);
                    }
                }
            }
        };
    });