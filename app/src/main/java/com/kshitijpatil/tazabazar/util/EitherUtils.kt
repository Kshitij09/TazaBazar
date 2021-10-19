package com.kshitijpatil.tazabazar.util

import arrow.core.Either
import arrow.core.identity

fun <A, B> Either<A, B>.getOrNull(): B? = fold({ null }, ::identity)