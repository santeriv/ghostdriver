var ghostdriver = ghostdriver || {};

ghostdriver.WebElementReqHand = function(id, session) {
    // private:
    var
    _id = id + '',      //< ensure this is always a string
    _session = session,
    _protoParent = ghostdriver.WebElementReqHand.prototype,
    _const = {
        VALUE           : "value",
        SUBMIT          : "submit"
    },

    _handle = function(req, res) {
        _protoParent.handle.call(this, req, res);

        // TODO lots to do...

        if (req.urlParsed.file === _const.VALUE && req.method === "POST") {
            _valueCommand(req, res);
            return;
        } else if (req.urlParsed.file === _const.SUBMIT && req.method === "POST") {
            _submitCommand(req, res);
            return;
        } // else ...

        // TODO lots to do...

        throw new ghostdriver.InvalidCommandMethod(req);
    },

    _valueCommand = function(req, res) {
        var i, ilen,
            postObj = JSON.parse(req.post);

        // Ensure all required parameters are available
        if (typeof(postObj) === "object" && typeof(postObj.value) === "object") {
            if (!_isAttachedToDOM())
                throw new ghostdriver.StaleElementReference(JSON.stringify(req));

            if (!_isVisible())
                throw new ghostdriver.ElementNotVisible(JSON.stringify(req));

            // For every String in the "value" array...
            for (i = 0, ilen = postObj.value.length; i < ilen; ++i) {
                _getSession().getCurrentWindow().evaluateWithParams(function(elementId, valueToAppend) {
                    document.querySelector("#"+elementId).value += valueToAppend;
                }, _getId(), postObj.value[i]);
            }

            res.statusCode = 200;
            res.closeGracefully();
            return;
        }

        throw new ghostdriver.MissingCommandParameters(req);
    },

    _submitCommand = function(req, res) {
        if (!_isAttachedToDOM())
            throw new ghostdriver.StaleElementReference(JSON.stringify(req));

        // Listen for the page to Finish Loading after the submit
        _getSession().getCurrentWindow().onLoadFinished = function(status) {
            if (status === "success") {
                res.statusCode = 200;
                res.closeGracefully();
            }

            // TODO - what do we do if this fails?
            // TODO - clear thing up after we are done waiting
        };

        // Submit element: either it's a form or an element in a form
        _getSession().getCurrentWindow().evaluateWithParams(function(elementId) {
            var el = document.getElementById(elementId);
            if (el.tagName === "FORM") {
                el.submit();
            } else if (el.form) {
                el.form.submit();
            }
        }, _getId());
    },

    _isAttachedToDOM = function() {
        return _getSession().getCurrentWindow().evaluateWithParams(function(elementId) {
            if (document.querySelector("#"+elementId))
                return true;
            return false;
        }, _getId());
    },

    _isVisible = function() {
        if (!_isAttachedToDOM())
            return false;

        return _getSession().getCurrentWindow().evaluateWithParams(function(elementId) {
            var el = document.querySelector("#"+elementId);
            if (el && el.style.visibility !== "hidden" && el.style.width >= 0 && el.style.height >= 0)
                return true;
            return false;
        }, _getId());
    },

    _getJSON = function() {
        return {
            "ELEMENT" : _getId()
        };
    },

    _getId = function() { return _id; },
    _getSession = function() { return _session; };

    // public:
    return {
        handle : _handle,
        getId : _getId,
        getJSON : _getJSON,
        getSession : _getSession,
        isAttachedToDOM : _isAttachedToDOM,
        isVisible : _isVisible
    };
};
// prototype inheritance:
ghostdriver.WebElementReqHand.prototype = new ghostdriver.RequestHandler();