fun main(args: Array<String>) {
    val p = Program(Add(Num(5), Num(22))
    )
    Compiler().compile(p)
}

/**
 * Interprets program.
 * @param program Program to interpret.
 * @return Return value.
 */
fun interpP(program: Program): Int {
    return interpE(program.e)
}

/**
 * Interprets expression.
 * @param e Expression to interpret.
 * @return Return value.
 */
fun interpE(e: Expr): Int {
    return e.eval(hashMapOf())
}

