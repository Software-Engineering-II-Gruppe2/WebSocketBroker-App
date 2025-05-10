package at.aau.serg.websocketbrokerdemo.data.properties

fun Property.copyWithOwner(newOwnerId: String?): Property {
    return when (this) {
        is HouseableProperty -> this.copy(ownerId = newOwnerId)
        is TrainStation -> this.copy(ownerId = newOwnerId)
        is Utility -> this.copy(ownerId = newOwnerId)
        is DummyProperty -> this.copy(ownerId = newOwnerId)
    }
}
