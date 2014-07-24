var app_id = '538356436272071';
var app_secret = 'cadfa5212bf95c5e1001911d0b3adf07';
var fb_callback_url = 'http://local.creator.why.academy/auth/facebook/callback';
//var api_url = "http://api.why.academy";
var api_url = "http://localhost:8080";

var qs = require('querystring');
var request = require('request');
var passport = require('passport')
    , FacebookStrategy = require('passport-facebook').Strategy;

var getUserToken = function(fbToken, callback) {

    var authentication = {'access_token': fbToken};
    console.log("Authentication: " + JSON.stringify(authentication));
    var params = {'authentication': JSON.stringify(authentication)};
    console.log("Param: " + params);
    var url = api_url + '/login/facebook?';
    url += qs.stringify(params);
    console.log("Url :" + url);
    request.post({
        headers: {'content-type': 'application/x-www-form-urlencoded'},
        url: url
    }, function(error, response, body) {
        console.log("Error: " + error);
        console.log("Response: " + JSON.stringify(response));
        console.log("Body: " + body);

        var data = JSON.parse(body);
        var userToken = {
            value: data.access_token,
            expires: data.expires
        };
        var user = {
            token: userToken,
            profile: data.user
        };
        console.log("profile: " + JSON.stringify(data.user));
        callback(user);

    });
};

var verifyHandler = function(token, tokenSecret, profile, done) {
    console.log("verify handler");
    process.nextTick(function() {
        console.log("Getting user why.academy information");
        getUserToken(token, function(user) {
            console.log("Getting user why.academy information [DONE]");
            return done(null, user);
            /*
             User.findOne({uid: profile.id}).done(function(err, user) {
             if (user) {
             return done(null, user);
             } else {

             var data = {
             uid: profile.id,
             name: profile.name,
             user_token: user_token,
             user_token_expires: user_token_expires
             };
             if (profile.email) {
             data.email = profile.email;
             }
             if (profile.first_name) {
             data.fistname = profile.first_name;
             }
             if (profile.last_name) {
             data.lastname = profile.last_name;
             }
             if (profile.picture_url) {
             data.picture_url = profile.picture_url;
             }
             if (profile.gender) {
             data.gender = profile.gender;
             }
             if (profile.locale) {
             data.locale = profile.locale;
             }
             console.log("Data: " + JSON.stringify(data));
             User.create(data).done(function(err, user) {
             return done(err, user);
             });
             }
             });
             });
             */

        });
    });
};
passport.serializeUser(function(user, done) {
    // user.user_token = JSON.stringify(user.user_token);
    done(null, user);
});
passport.deserializeUser(function(user, done) {
    //User.findOne({uid: uid}).done(function(err, user) {
    done(null, user);
    //});
});
module.exports = {
    // Init custom express middleware
    express: {
        customMiddleware: function(app) {


            passport.use(new FacebookStrategy({
                    clientID: app_id,
                    clientSecret: app_secret,
                    callbackURL: fb_callback_url
                },
                verifyHandler
            ));
            app.use(passport.initialize());
            //app.use(passport.session());

            var express = require('express');
            var path = require('path');

            // app.use(express.static(path.resolve(__dirname, '..', 'assets')));
            // app.use('/app', express.static(path.resolve(__dirname, '..', '..', 'angular', 'dev', 'src', 'app')));

            app.get('*', function(req, res, next){
                var routes = req.url.split('/');
                var i = 0;
                var statics = [
                    'api',
                    'js',
                    'css',
                    'styles',
                    'images',
                    'vendor',
                    'controllers',
                    'views',
                    'commons',
                    'login',
                    'logout',
                    'auth'
//                    ,'admin'
                ];
                var isStatic = false;
                for(i = 0; i < statics.length; i++){
                    if(routes[1] === statics[i]){
                        isStatic = true;
                    }
                }
                if (isStatic) {
                    return next();
                };
                return res.render(path.resolve(__dirname, '..', 'views', 'home', 'index.html'));
            });
        }
    }

};