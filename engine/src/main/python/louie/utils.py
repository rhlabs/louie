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

"""Utilities for encoding/decoding protocol buffers, not present in google's protobuf package

   Code copied from: google.protobuf.internal.encoder
                     google.protobuf.internal.decoder
"""
__all__ = ['encodeVarint', 'decodeVarint32', 'decodeVarint64',
           'decodeVarint32FromHttp', 'decodeVarint64FromHttp', 
           'encodeDelimited', 'decodeDelimitedFromHttp']

# ========================
# PRIVATE CLASSES (Google)
# ========================  

def _VarintEncoder():
    """Return an encoder for a basic varint value (does not include tag)."""

    local_chr = chr
    def EncodeVarint(write, value):
        bits = value & 0x7f
        value >>= 7
        while value:
            write(local_chr(0x80|bits))
            bits = value & 0x7f
            value >>= 7
        return write(local_chr(bits))

    return EncodeVarint

def _VarintDecoder(mask):
    """Return an encoder for a basic varint value (does not include tag).

    Decoded values will be bitwise-anded with the given mask before being
    returned, e.g. to limit them to 32 bits.  The returned decoder does not
    take the usual "end" parameter -- the caller is expected to do bounds checking
    after the fact (often the caller can defer such checking until later).  The
    decoder returns a (value, new_pos) pair.
    """

    local_ord = ord
    def DecodeVarint(buffer, pos):
        result = 0
        shift = 0
        while 1:
            b = local_ord(buffer[pos])
            result |= ((b & 0x7f) << shift)
            pos += 1
            if not (b & 0x80):
                result &= mask
                return (result, pos)
            shift += 7
            if shift >= 64:
                raise _DecodeError('Too many bytes when decoding varint.')
    return DecodeVarint

# =======================
# PRIVATE CLASSES (RandH)
# =======================  

def _HttpVarintDecoder(mask):
    """Encoder based on Google's VarintDecoder, just using httpResponse instead
       of byte array.
    """

    local_ord = ord
    def DecodeVarint(httpResponse):
        result = 0
        shift = 0
        while 1:
            b = local_ord(httpResponse.read(1))
            result |= ((b & 0x7f) << shift)
            if not (b & 0x80):
                result &= mask
                return (result)
            shift += 7
            if shift >= 64:
                raise _DecodeError('Too many bytes when decoding varint.')
    return DecodeVarint

# =============================================================================
# GLOBALS
# =============================================================================

_EncodeVarint = _VarintEncoder()
_DecodeVarint64 = _VarintDecoder((1 << 64) - 1)
_DecodeVarint32 = _VarintDecoder((1 << 32) - 1)
_HttpDecodeVarint64 = _HttpVarintDecoder((1 << 64) - 1)
_HttpDecodeVarint32 = _HttpVarintDecoder((1 << 32) - 1)

# =======================
# PUBLIC METHODS (Google)
# =======================  

# Renamed from _VarintBytes
def encodeVarint(value):
    """Encode the given integer as a varint and return the bytes.  This is only
    called at startup time so it doesn't need to be fast."""

    pieces = []
    _EncodeVarint(pieces.append, value)
    return "".join(pieces)


# =======================
# PUBLIC METHODS (RandH)
# =======================

def encodeDelimited(pb):
    """Encode the pb, preceeding it with the size of the serialized pb as a varint
    """ 
    encoded = pb.SerializeToString()
    return encodeVarint(len(encoded)) + encoded;


def decodeDelimitedFromHttp(response, decodeFunction=None):
    """Decode a PB from an HTTP response, expecting the size first as a varint
    """ 
    pbSize = decodeVarint32FromHttp(response)

    buffer = ''
    bufSize = 0
    streamSize = 50
    while pbSize > bufSize:
        if pbSize - bufSize >= streamSize:
            stream = response.read(streamSize)
        else:
            stream = response.read(pbSize - bufSize)   
        if not stream:
            break

        buffer += stream
        bufSize = len(buffer)

    if decodeFunction:
         return decodeFunction(buffer)
    return buffer

def decodeVarint32(bytes, pos=0): 
    """Decode an 32 bit integer from a varint byte representation.
       Expects an array of bytes to read from, and an optional start position. 
    """
    return _DecodeVarint32(bytes,pos)

def decodeVarint64(bytes, pos=0):
    """Decode an 32 bit integer from a varint byte representation.
       Expects an array of bytes to read from, and an optional start position.
    """
    return _DecodeVarint64(bytes,pos)

def decodeVarint32FromHttp(response):
    """Decode an 32 bit integer from a varint byte representation.
       Expects an HTTP Response to read from.
    """
    return _HttpDecodeVarint32(response)

def decodeVarint64FromHttp(response):
    """Decode an 64 bit integer from a varint byte representation.
       Expects an HTTP Response to read from.
    """
    return _HttpDecodeVarint64(response)
