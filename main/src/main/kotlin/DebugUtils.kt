fun powerOfTwoGenerator(power: Int): Program {
    val numTwos = Math.pow(2.toDouble(), power.toDouble()) / 2

    return Program(Write(twoHelper(numTwos.toInt())))
}

fun twoHelper(amtLeft: Int): Expr {
    if (amtLeft <= 0) {
        return Num(0)
    }
    return Add(Num(2), twoHelper(amtLeft - 1))
}