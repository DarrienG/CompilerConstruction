import org.junit.Test

class EvalTest {
    @Test
    fun testAddNegLetVarNum() {
        val p = Program(Add(
                Let("y",
                        Let(
                                "x",
                                Neg(Num(5)),
                                Write(Var(Type.INT,"x"))
                        ), Add(Var(Type.INT, "x"), Num(5))),
                Write(Var(Type.INT,"y"))))

        val result = interpP(p)
        assert(result == -5)
        p.uniquify()
        assert(interpP(p) == -5)
        val c = p.flatten()
        println(c)
        Compiler().compile(p, timed = true, toFile = true, numRegs = NumRegs.CALLEE)
    }

    @Test(expected = RuntimeException::class)
    fun testVarNotInitialized() {
        interpP(Program(
                Let("x", Num(5), Add(Var(Type.INT,"y"), Num(10)))))
    }

    @Test
    fun testLet() {
        val p = Program(
                Let("x", Num(5),
                        Let("x", Num(27),
                                Add(Var(Type.INT,"x"), Num(12))))
        )
        assert(interpP(p) == 39)
        p.uniquify()
        assert(interpP(p) == 39)
        Compiler().compile(p, timed = true, toFile = true, numRegs = NumRegs.NONE)
    }

    @Test
    fun doTheThing() {
        val p = Program(Let("x", Add(Num(5), Num(10)),
                Add(Var(Type.INT, "x"), Num(13))))

        Compiler().compile(p)
        println(interpP(p))
    }

    @Test(expected = RuntimeException::class)
    fun testCond() {
        val c = Compiler()
        var p = Program(Bool("t"))
        assert(interpP(p) == 1)

        // Test if true
        p = (Program(
                If(Bool("t"), Num(5), Num(600)
                        )))
        assert(interpP(p) == 5)

        // Test if false
        p = (Program(
                If(Bool("f"), Num(5), Num(600)
                )))
        assert(interpP(p) == 600)

        // Test not
        p = (Program(
                If((Not(Bool("f"))), Num(5), Num(600)
                )))
        assert(interpP(p) == 5)
        c.compile(p, toFile = true)

        p = (Program(
                Comp(CmpType.LT, Num(5), Num(22))
        ))
        assert(interpP(p) == 1)

        p = (Program(
                Comp(CmpType.NZER, Num(5), Num(22))
        ))
        assert(interpP(p) == 1)

        p = (Program(
                If(Comp(CmpType.GT, Num(5), Num(17)),
                        Num(13), Num(25))
        ))
        assert(interpP(p) == 25)

        p = (Program(
                If(Bool("f"), Bool("f"), Num(600)
                )))
        // Will throw exception because both sides must return the same type
        interpP(p)
    }

    @Test(expected = RuntimeException::class)
    fun testLetTypeChecker() {
        val p = Program(
                Let("y", Add(Num(5), Num(32)),
                        Not(Var(Type.BOOL, "y"))))
        interpP(p)
    }

    @Test
    fun testVecInterp() {
        val p = Program(Let("bubber", Vector(
                mutableListOf(Num(1),
                        Neg(Num(2)),
                        Bool("t"),
                        Add(Num(2), Num(3)))
        ),
                Add(VectorRef(Var(Type.VECTOR, "bubber"), Num(3)), Num(4))))
        assert(interpP(p) == 9)
        Compiler().compile(p)
    }
}