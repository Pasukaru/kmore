package my.company.app

import java.time.Instant

fun log(msg: Any?) {
    println("${Instant.now()} [${Thread.currentThread().name.padEnd(27, ' ')}]: $msg")
}