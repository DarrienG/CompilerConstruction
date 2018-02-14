fun main(args: Array<String>) {
    val p = Program(Write(Add(Num(5), Num(32))))
    println(interpP(p))
    Compiler().compile(p, timed = true)
}

fun interpP(program: Program): Int {
    return interpE(program.e)
}

fun interpE(e: Expr): Int {
    return e.eval(hashMapOf())
}

