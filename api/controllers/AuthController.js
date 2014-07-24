
var arena_url;
var api_url;
var loadEnv = function() {
    console.log("Running enviroment: " + process.env.NODE_ENV);
    if (process.env.NODE_ENV === "development") {
        arena_url = 'http://localhost';
        api_url = 'http://staging.why.academy:8080';
    }
    else {
        arena_url = 'http://arena.why.academy';
        api_url = 'http://api.why.academy';
    }
};
loadEnv();

var redirect_url = arena_url + '/auth/facebook';


var client_id = '319210081588306';
var client_secret = 'b2d0677081814edd3edef89eee265e74';


var qs = require('querystring');
var request = require('request');
var FB = require('fb');

var getFacebookAccessToken = function(code, callback) {
    var accessToken;
    var expires;

    FB.api('oauth/access_token', {
        client_id: client_id,
        client_secret: client_secret,
        redirect_uri: redirect_url,
        code: code
    }, function(res) {
        if (!res || res.error) {
            console.log(!res ? 'error occurred' : res.error);
            return;
        }
        accessToken = res.access_token;
        expires = res.expires ? res.expires : 0;
        console.log("Access token: " + accessToken);
        console.log("Expires: " + expires);

        callback(accessToken);
    });
};

var login = function(fbToken, callback) {

    var params = {'fb_access_token': fbToken};
    console.log("Param: " + params);
    var url = api_url + '/v2/login/facebook?';
    url += qs.stringify(params);
    console.log("Url :" + url);
    request.get({
        headers: {'content-type': 'application/x-www-form-urlencoded'},
        url: url
    }, function(error, response, body) {
        console.log("Error: " + error);
        console.log("Response: " + response);
        console.log("Body: " + body);

        var data = JSON.parse(body);
        var userToken = {
            value: data.auth.access_token,
            expires: data.auth.expires_at
        };
        var user = {
            token: userToken,
            profile: data.user
        };
        console.log("profile: " + JSON.stringify(data.user));
        callback(user);
    });
};

var AuthController = {
    login: function(req, res) {
        var provider = req.param('provider');
        if (provider === "facebook") {
            res.redirect('https://www.facebook.com/dialog/oauth?' +
                    'client_id=' + client_id +
                    '&redirect_uri=' + redirect_url +
                    '&scope=email%2Cpublish_stream&state=123456789');
            return;
        }
    },
    facebook: function(req, res) {
        var code = req.param('code');
        getFacebookAccessToken(code, function(fb_token) {
            login(fb_token, function(user) {
                var expires = new Date(user.token.expires);
                var now = new Date();
                var expiresTime = expires.getTime() - now.getTime();
                
                res.cookie('user', user, {maxAge: expiresTime});
                console.log("Cookie already set");
                res.redirect(arena_url);
                return;
            });
        });
    },
    logout: function(req, res) {
        console.log("Do logout");
        res.clearCookie('user');
        res.redirect(arena_url);
        return;
    }
};

module.exports = AuthController;
 