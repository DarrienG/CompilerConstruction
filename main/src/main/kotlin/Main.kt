fun main(args: Array<String>) {
    val p = powerOfTwoGenerator(7)
    Compiler().compile(p, toFile = true, timed = true)
}

fun interpP(program: Program): Any {
    return interpE(program.e)
}

fun interpE(e: Expr): Any {
    return e.eval(hashMapOf())
}

