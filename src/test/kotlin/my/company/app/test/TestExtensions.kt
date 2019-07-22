package my.company.app.test

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.ktor.http.HttpStatusCode

fun HttpStatusCode?.expectCreated() = assertThat(this).isEqualTo(HttpStatusCode.Created)
fun HttpStatusCode?.expectOK() = assertThat(this).isEqualTo(HttpStatusCode.OK)
