package com.retoday.core.global.extension

import com.fasterxml.uuid.Generators
import java.util.*

fun createUuid(): UUID =
    Generators.timeBasedGenerator()
        .generate()
