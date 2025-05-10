package at.aau.serg.websocketbrokerdemo.data.properties

data class TrainStation(
    override val id: Int,
    override val name: String,
    override val purchasePrice: Int,
    override val position: Int,
    val baseRent: Int,
    val rent2Stations: Int,
    val rent3Stations: Int,
    val rent4Stations: Int,
    override val image: String,
    override val isMortgaged: Boolean, override var ownerId: String?
) : Property()
