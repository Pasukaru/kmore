package server.lib.repository

import org.kodein.di.KodeinAware
import server.KtorMain

abstract class AbstractRepository : KodeinAware {
    override val kodein by lazy { KtorMain.kodein }
}