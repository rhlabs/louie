# Copyright 2015 Rhythm & Hues Studios.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

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

