package at.aau.serg.websocketbrokerdemo.data.properties

data class HouseableProperty(
    override val id: Int,
    override val name: String,
    override val purchasePrice: Int,
    override val position: Int,
    val baseRent: Int,
    val rent1House: Int,
    val rent2Houses: Int,
    val rent3Houses: Int,
    val rent4Houses: Int,
    val rentHotel: Int,
    val housePrice: Int,
    val hotelPrice: Int,
    val mortgageValue: Int,
    override val image: String,
    override val isMortgaged: Boolean, override var ownerId: String?
) : Property()
