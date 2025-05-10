package at.aau.serg.websocketbrokerdemo.data.messages

data class TaxPaymentMessage(
  override  val type: String = "TAX_PAYMENT",
    val playerId: String,
    val amount: Int,
    val taxType: String  // "EINKOMMENSTEUER" or "ZUSATZSTEUER"
):GameMessage
