fun main(args: Array<String>) {
    val p = Program(Add(Num(5), Num(22))
    )
    Compiler().compile(p)
}

fun interpP(program: Program): Int {
    return interpE(program.e)
}

fun interpE(e: Expr): Int {
    return e.eval(hashMapOf())
}

