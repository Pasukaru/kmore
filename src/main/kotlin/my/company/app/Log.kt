package my.company.app

import java.time.Instant

private object LOG {
    const val THREAD_NAME_LENGTH = 27
}

fun log(msg: Any?) {
    println("${Instant.now()} [${Thread.currentThread().name.padEnd(LOG.THREAD_NAME_LENGTH, ' ')}]: $msg")
}
