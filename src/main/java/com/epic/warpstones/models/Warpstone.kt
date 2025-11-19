package com.epic.warpstones.models

import java.util.UUID

class Warpstone {
    public var x: Double = 0.0
    public var y: Double = 0.0
    public var z: Double = 0.0

    public var name: String = ""
    public var destination: String = ""
    public var owner: UUID? = null
}