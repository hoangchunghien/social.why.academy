var CoursesController = {
    index: function(req, res) {
        return res.view({
            
        });
    },
    
    detail: function(req, res) {
        return res.view({
            id : req.param('id')
        });
    },

    edit: function(req, res) {
        return res.view({

        });
    }
};

module.exports = CoursesController;
