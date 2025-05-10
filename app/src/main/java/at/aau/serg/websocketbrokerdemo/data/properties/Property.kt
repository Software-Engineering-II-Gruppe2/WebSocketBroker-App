package at.aau.serg.websocketbrokerdemo.data.properties

sealed class Property {
    abstract val id: Int
    abstract var ownerId: String?
    abstract val name: String
    abstract val purchasePrice: Int
    abstract val position: Int
    abstract val image: String
    abstract val isMortgaged: Boolean
}

data class DummyProperty(
    override val id: Int,
    override val position: Int,
    val color: PropertyColor,
    override var ownerId: String? = null
) : Property() {
    override val name = "dummy"
    override val purchasePrice = 0
    override val image = ""
    override val isMortgaged = false
}
