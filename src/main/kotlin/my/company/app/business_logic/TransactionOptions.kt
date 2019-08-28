package my.company.app.business_logic

import my.company.app.db.IsolationLevel

data class TransactionOptions(
    val isolationLevel: IsolationLevel = IsolationLevel.READ_COMMITTED,
    val readOnly: Boolean = false,
    val inherit: Boolean = true
) {
    companion object {
        val DEFAULT = TransactionOptions()
    }
}
