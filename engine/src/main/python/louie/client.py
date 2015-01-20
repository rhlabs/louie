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

"""Client for communicating with a Louie server

Classes:
    LouieHttpClient
        Generic client for Louie via HTTP

"""

# =============================================================================
# IMPORTS
# =============================================================================

import sys
import os
import socket
from threading import Lock
import time
import getpass
import logging

from louie.http import HTTPConnection, HTTPSConnection, HTTPException, BadStatusLine
from louie.request_pb2 import SessionKey, IdentityPB
from louie.request_pb2 import RequestPB, RequestHeaderPB
from louie.request_pb2 import ResponsePB, ResponseHeaderPB
from louie.utils import encodeDelimited, decodeDelimitedFromHttp

__all__ = [
    "LouieHttpClient"
]

# =============================================================================
# GLOBALS
# =============================================================================

_LOCK = Lock()
SERV_RESP_OKAY = 200
SERV_RESP_NOT_FOUND = 404
SERV_RESP_SERVICE_UNAVAILABLE = 503
SERV_RESP_PROXY_AUTHENTICATION_REQUIRED = 407

# =============================================================================
# CLASSES
# =============================================================================

LOGGER = logging.getLogger('louie');

LOUIE_DEBUG = os.environ.get('LOUIE_DEBUG', 'false').lower() in ("yes", "true", "t", "1")
if LOUIE_DEBUG:
    print "Louie Debug mode Enabled"
    LOGGER.setLevel(logging.DEBUG)

class LouieHttpClient(object):
    """HTTP interface to the Louie server
    """
    _instances = {}
    
    def __new__(cls, host='louiehost', port='8080', gateway='louie',
                authport='8787'):
        with _LOCK:
            try:
                return cls._instances[host]
            except KeyError:
                client = super(LouieHttpClient, cls).__new__(cls)
                cls._instances[host] = client
                return client
            
    def __init__(self, host='louiehost', port='8080', gateway='louie',
                authport='8787'):
        try:
            self._initialized
        except AttributeError:
            pass
        else:
            return
        
        if not host: 
            raise TypeError('No host specified.')
        if not port: 
            raise TypeError('No port specified.')
        if not gateway:
            raise TypeError('No gateway specified.')

        self._key = None
        self._host = host
        self._port = port
        self._authport = authport
        
        gateway = gateway.lstrip('/').rstrip('/')
        self._gateway = "/{0}/pb".format(gateway)

        self._identity = IdentityPB()
        self._identity.language = 'python/{0}'.format(sys.version[:3])
        self._identity.user = getpass.getuser()

        path = sys.argv[0]
        self._identity.program = os.path.basename(path)
        self._identity.path = os.path.abspath(path)
        self._identity.processId = str(os.getpid())
    
        if socket.gethostname() is not None:
            self._identity.machine = socket.gethostname()
        else:
            self._identity.machine = ""

        if LOGGER.isEnabledFor(logging.DEBUG): 
            LOGGER.debug(str(self._identity))

        self._doRetry = True
        self._lockOffRetry = False
        self._retrySeconds = 120
        
        self._connection = None
        secured = os.environ.get("LOUIE_SECURE")
        self._secured = secured.lower() in ("yes", "true", "t", "1") if secured is not None else False
        if self._secured:
            self._securePort = os.environ.get("LOUIE_SECURE_PORT")
            if self._securePort is None:
                raise ConfigurationError("Failed to set a secure port for https! Please specify a LOUIE_SECURE_PORT env variable")
            homedir = os.path.expanduser("~")
            self._secureCert = homedir + "/louie_client.crt"
            self._secureKey = homedir + "/louie_client.key"
        
        #specialized auth behavior
        self._authEnabled = False
        
        self._initialized = True

    # =======================
    # PUBLIC METHODS
    # =======================  
        
    def request(self, system, method, params=None, decodeFunction=None):
        """Perform a request to the Louie server.  Params should be an array
           of the arguments that the method takes.  If a decodeFunction is
           specified then it will be used on each binary block and the results
           of that decoding will be returned instead of the raw binary.

           This method also implements the retry logic, which is configurable
           via setRetry() and setRetryTimeoutWindow()
           
           Example usage :
                jobname = StringPB()
                jobname.value = "lop"
                jobPb = request(
                    system="job",
                    method="getJob",
                    params=[jobname],
                    decodeFunction=JobPB.FromString
                )
        """
        
        elapsedTime = 0
        sleepIncr = 1
        start = time.time()
        
        if not params:
            params = []
        elif not isinstance(params, list):
            params = [params]
        
        while True:
            try:
                if LOGGER.isEnabledFor(logging.DEBUG):
                    paramString = ''                
                    for i in params:
                        paramString += str(i) + ","
                    
                    LOGGER.debug("Louie Request: %s:%s - %s", system, method, paramString)
                    
                res = self._doRequest(system, method, params, decodeFunction)
                self._lockOffRetry = False
                return res
            except (ConnectError, StatusLineError, RequestError, AuthenticationError) as e:
                if (elapsedTime >= self._retrySeconds) or not self._doRetry or self._lockOffRetry:
                    self._lockOffRetry = True
                    raise e
                
                if LOGGER.isEnabledFor(logging.WARNING):
                    warning = "{err} Reattempting request (timeout remaining: {time}s)".format(
                        err = str(e),
                        time = str(self._retrySeconds - elapsedTime)
                        )
                    LOGGER.warning(warning)
                    
                time.sleep(sleepIncr)
                elapsedTime = round(time.time() - start)
                #ridiculous increasing retry sleep time logic: 1,2,4,5,5,5...
                sleepIncr = (sleepIncr*2) if sleepIncr <= 5 else 5            
            
    def setRetryTimeoutWindow(self, retrySeconds):
        """ Set the window (in seconds) during which any request will be
        retried. Initialized to 120s. After the timeout window, no additional
        retries will be reattempted and any error caught is immediately thrown.
        """
        self._retrySeconds = retrySeconds

    def setRetry(self, retry=True):
        """ Enable or disable the retry loop functionality. Connections/requests
        will be reattempted up to a maximum period defined by self._retrySeconds
        If disabled, any errors caught are immediately thrown.
                        ** Enabled by default **
        """
        self._doRetry = retry
        
    def enableAuthBehavior(self, enable=True):
        """ Enable or disable specialized first-call auth behavior, where the
        initial call will happen on a specified auth port, and every subsequent
        call on the normal port.
        """
        self._authEnabled = enable
        
    def setCertPath(self, certPath):
        """ Full path of an x509 certificate """
        self._securePath = certPath
        
    def setKeyPath(self, keyPath):
        """ Full path of a private key """
        self._secureKey = keyPath
        
    # =======================
    # PRIVATE METHODS
    # =======================  

    # -----------------------------------------------------------------------------
    #    Name: _createConnection
    #  Raises: ConnectError
    # Returns: HTTPConnection
    #    Desc: Establishes an HTTP connection to the Louie server, possibly over
    #          specified auth port
    # -----------------------------------------------------------------------------    
    def _createConnection(self):
        
        if self._authEnabled and not self._key:
            #handle new connections over auth port if no key has been set
            targetPort = self._authport
        else:
            targetPort = self._port
        try:
            conn = HTTPConnection(self._host, targetPort)
        except (HTTPException, socket.error, socket.timeout, socket.herror,
                socket.gaierror) as e:
            # report all error messages from server
            errStr = 'Unable to connect host {host}:{port}: {errs}'.format(
                host=self._host,
                port=targetPort,
                errs=' '.join([str(arg) for arg in e.args])
            )
            raise ConnectError(errStr)
        return conn
    
    def _createSecureConnection(self):
        
        try:
            conn = HTTPSConnection(self._host, self._securePort,
                self._secureKey, self._secureCert)
        except (HTTPException, socket.error, socket.timeout, socket.herror,
                socket.gaierror) as e:
            # report all error messages from server
            errStr = 'Unable to connect host {host}:{port}: {errs}'.format(
                host=self._host,
                port=targetPort,
                errs=' '.join([str(arg) for arg in e.args])
            )
            raise ConnectError(errStr)
            
        return conn

    # -----------------------------------------------------------------------------
    #    Name: _doRequest
    #    Args: service: (str)
    #              The name of the louie service
    #          method : (str)
    #              The service function name
    #          params=[] : ([PB] or PB)
    #              The arguments to the function
    #          decodeFunction=None : function
    #              function to parse the binary data into PBs
    #  Raises: TypeError, StatusLineError, SocketError, AuthenticationError
    #              RequestError, ServerError
    # Returns: [binary] or [PB]
    #              Returns an array of serialized PBs.  If a decodeFunction is 
    #              specified then that is ran on each binary block and the 
    #              resultant PB objects are returned instead
    #    Desc: Encodes a request to the Louie server over an http connection.
    #          Returns the resultant data in binary form or the decoded PBs
    #          if a decodeFunction is provided
    # -----------------------------------------------------------------------------
    def _doRequest(self, service, method, params=None, decodeFunction=None):
        if not isinstance(params, list):
            raise TypeError('params must be passed as a list.')
        
        # request header
        header = RequestHeaderPB()
        header.count = 1
        if self._key: # we've already gotten a key back: this isn't first run
            header.key.key = self._key
        else:         # this is the first run
            header.identity.CopyFrom(self._identity)
        # request body
        request = RequestPB()
        request.id = 1
        request.service = service
        request.method = method
        request.param_count = 1
        for param in params:
            description = param.DESCRIPTOR.full_name
            request.type.append(description)
                    
        # encode the request    
        encodedRequest = encodeDelimited(header) + encodeDelimited(request)
        for param in params:
            encodedRequest +=  encodeDelimited(param)
        #get a connection
        if (self._secured):
            connection = self._createSecureConnection()
        else:
            connection = self._createConnection()
        connection.connect()
        
        connection.request(
            "POST", 
            self._gateway,
            encodedRequest,
            {"Content-type": "application/x-protobuf"}
        )
        try:
            response = connection.getresponse()                            
        except (BadStatusLine, socket.error) as e:
            connection.close()
            if isinstance(e, BadStatusLine):
                raise StatusLineError("Bad Status Line Error")
            else:
                LOGGER.error("Unexpected/Fatal Socket Error encountered " + str(e))
                raise SocketError("Fatal Socket Error")
        
        status = response.status
        
        if ( status != SERV_RESP_OKAY ):
            if (status == SERV_RESP_PROXY_AUTHENTICATION_REQUIRED):
                self._key = None
                errStr = "Authentication Error: {code} {reason}".format(
                    code = str(status),
                    reason = response.reason)
                raise AuthenticationError(errStr)
            
            elif ( status == SERV_RESP_NOT_FOUND or
                status == SERV_RESP_SERVICE_UNAVAILABLE ):
                errStr = "Request Error: {code} {reason} ".format( 
                        code = str(status),
                        reason = response.reason)
                #Close the connection each time, else the response still pending
                connection.close()
                raise RequestError(errStr) #this error prompted a retry, before

            else:
                errStr = "Request Error: {code} {reason}".format( 
                    code = str(status),
                    reason = response.reason)
                raise ServerError(errStr)

        resHeader = decodeDelimitedFromHttp(response, ResponseHeaderPB.FromString)
        
        # if server sent back a key, it's mine
        if resHeader.key.key:
            self._key = resHeader.key.key

        # get all the responses
        results = []
        for respCnt in range(resHeader.count):
            resBody = decodeDelimitedFromHttp(response, ResponsePB.FromString)
            if resBody.error.description:
                raise LouieError(resBody.error.description)
            for pbCnt in range(resBody.count):
                results.append(decodeDelimitedFromHttp(response, decodeFunction))
 
        connection.close()
        
        return results
    
# =======================
# EXCEPTIONS
# =======================  
 
class ServerError(StandardError):
    """Report all server errors."""
    pass

class LouieError(ServerError):
    """Louie generated error"""
    pass

class ConnectError(ServerError):
    """Error Connecting to the Server."""
    pass

class RequestError(ServerError):
    """Error building request."""
    pass

class StatusLineError(ServerError):
    """Bad Status Line caught and re-thrown"""
    pass

class AuthenticationError(ServerError):
    """Error Authenticating the Session."""
    pass

class SocketError(ServerError):
    """Fatal socket error."""
    pass
        
class ConfigurationError(ServerError):
    """Improper configuration"""
    pass
        
