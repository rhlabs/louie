"""Exposes all classes from httplib as-is with the exception of HTTPResponse,
which is implemented here as a subclass of httplib.HTTPResponse. 

The HTTPResponse implementation here is identical to that of 
httplib.HTTPResponse except that here the HTTPResponse object's socket 
connection is buffered (using the system's default bufsize).  This avoids 
explicit socket reads for each individual character regardless of the number of
requested bytes.
"""
__version__ = '${project.version}'
# =============================================================================
# IMPORTS
# =============================================================================

import httplib

# pylint: disable=W0401
from httplib import *

# Expose all members of httplib as members of this module.
__all__ = httplib.__all__

# =============================================================================
# CLASSES
# =============================================================================

class HTTPResponse(httplib.HTTPResponse):
    """Subclass of httplib.HTTPResponse that uses a buffered socket connection.
    """
    def __init__(self, sock, debuglevel=0, strict=0, method=None):
        httplib.HTTPResponse.__init__(self, 
                                      sock, 
                                      debuglevel=debuglevel, 
                                      strict=strict, 
                                      method=method)
        self.fp = sock.makefile('rb')

# =============================================================================
# EXECUTE UPON IMPORT
# =============================================================================

# Monkey-patch the HTTPConnection classe's response_class attribute so that 
# the HTTPConnection.getresponse() method will return an instance of our
# HTTPResponse class, rather than an instance of httplib.HTTPResponse.
HTTPConnection.response_class = HTTPResponse

