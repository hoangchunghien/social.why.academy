/**
 * Allow any authenticated user.
 */
module.exports = function(req, res, next)
{
    if (req.isAuthenticated())
        return next();

    res.redirect('/login');
};