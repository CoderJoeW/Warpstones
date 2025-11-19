package com.epic.warpstones.models

import java.util.UUID

class Warpstone {
    public var x: Int = 0
    public var y: Int = 0
    public var z: Int = 0

    public var name: String = ""
    public var destination: String = ""
    public var owner: UUID? = null
}