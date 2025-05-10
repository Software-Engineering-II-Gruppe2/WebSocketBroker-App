package at.aau.serg.websocketbrokerdemo.data.properties

data class Utility(
    override val id: Int,
    override val name: String,
    override val purchasePrice: Int,
    override val position: Int,
    val rentOneUtilityMultiplier: Int,
    val rentTwoUtilitiesMultiplier: Int,
    override val image: String,
    override val isMortgaged: Boolean, override var ownerId: String?
) : Property()
