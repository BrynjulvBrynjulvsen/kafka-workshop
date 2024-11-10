package tasks

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

fun doForDuration(duration: Duration, block: () -> Unit) {
    val end = TimeSource.Monotonic.markNow().plus(10.seconds)
    while (end.hasNotPassedNow()) {
        block()
    }

}