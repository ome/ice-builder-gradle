//
// Copyright (c) ZeroC, Inc. All rights reserved.
//

package com.zeroc.gradle.icebuilder.slice;

class Dict {
    final name
    def javaType
    def key
    def value
    // list of dictionary values
    def index

    Dict(String n) {
        name = n
    }
}
