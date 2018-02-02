import org.junit.Test

class EvalTest {
    @Test
    fun testAddNegLetVarNum() {
        val p = Program(Add(
                Let("y",
                        Let(
                                "x",
                                Neg(Num(5)),
                                Write(Var("x"))
                        ), Add(Var("x"), Num(5))),
                Write(Var("y"))))

        val result = interpP(p)
        assert(result == -5)
        p.uniquify()
        assert(interpP(p) == -5)
        val c = p.flatten()
        println(c)

    }

    @Test(expected = RuntimeException::class)
    fun testVarNotInitialized() {
        interpP(Program(
                Let("x", Num(5), Add(Var("y"), Num(10)))))
    }

    @Test
    fun testLet() {
        val p = Program(
                Let("x", Num(5),
                        Let("x", Num(27),
                                Add(Var("x"), Num(12))))
        )
        assert(interpP(p) == 39)
        p.uniquify()
        assert(interpP(p) == 39)
    }

    @Test
    fun doTheThing() {
        val p = Program(Add(
                Let("y",
                        Let(
                                "x",
                                Neg(Num(5)),
                                Var("x")
                        ), Add(Var("x"), Num(5))),
                (Var("y"))))

        Compiler().compile(p)
        println(interpP(p))
    }
}