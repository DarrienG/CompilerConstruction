fun main(args: Array<String>) {
    Compiler().compile(Program(Let("x", Num(5),
            Let("y", Add(Var("x"), Num(22)), Write(Var("y"))))))
}

fun interpP(program: Program): Int {
    return interpE(program.e)
}

fun interpE(e: Expr): Int {
    return e.eval(hashMapOf())
}

