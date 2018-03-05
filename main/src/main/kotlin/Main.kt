fun main(args: Array<String>) {
    val p = powerOfTwoGenerator(13)
    Compiler().compile(p, toFile = true, timed = true)
}

fun interpP(program: Program): Int {
    return interpE(program.e)
}

fun interpE(e: Expr): Int {
    return e.eval(hashMapOf())
}

