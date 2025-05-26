package at.aau.serg.websocketbrokerdemo.data.properties

// fixme avoid extension methods within self-owned modules
//  also, this method requires to know all subtypes - instead use polymorphism where required
fun Property.copyWithOwner(newOwnerId: String?): Property {
    return when (this) {
        is HouseableProperty -> this.copy(ownerId = newOwnerId)
        is TrainStation -> this.copy(ownerId = newOwnerId)
        is Utility -> this.copy(ownerId = newOwnerId)
        is DummyProperty -> this.copy(ownerId = newOwnerId)
    }
}
