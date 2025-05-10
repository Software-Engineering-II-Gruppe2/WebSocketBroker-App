package at.aau.serg.websocketbrokerdemo.data

data class TaxPaymentMessage(
    val type: String = "TAX_PAYMENT",
    val playerId: String,
    val amount: Int,
    val taxType: String  // "EINKOMMENSTEUER" or "ZUSATZSTEUER"
)
