/**
 * Argument valid for an C0 program statement.
 */
interface Argument {
    fun getVal(): Any
}

/**
 * Valid expression for a C0 program.
 */
interface CExpr {
    fun select(xp: XProgram, arg: Argument)
}

/**
 * Raw number in C0 program.
 */
data class CNum(private var n: Int = 314159): Argument {
    override fun getVal(): Any {
        return n
    }

}

/**
 * Variable in C0 language.
 * @param x Name of the variable.
 */
data class CVar(private var x: String): Argument {
    override fun getVal(): Any {
        return x
    }
}

/**
 * Valid statement in the C0 language.
 * @param x Variable where return data is held.
 * @param xe Expression run.
 */
data class CStmt(var x: CVar, val xe: CExpr)

data class CLet(private val a: Argument): CExpr {
    override fun select(xp: XProgram, arg: Argument) {
        val sentArg = convertCArgToXArg(a)
        xp.instrList.add(XMovq(sentArg, convertCArgToXArg(arg)))
    }
}

data class CNeg(private val a: Argument): CExpr {
    override fun select(xp: XProgram, arg: Argument) {
        val sentArg = convertCArgToXArg(a)
        val dest = convertCArgToXArg(arg)
        xp.instrList.add(XMovq(sentArg, dest))
        xp.instrList.add(XNegq(dest))
    }
}

data class CAdd(private val a: Argument, private val b: Argument): CExpr {
    override fun select(xp: XProgram, arg: Argument) {
        val sentArgA = convertCArgToXArg(a)
        val sentArgB = convertCArgToXArg(b)
        val dest = convertCArgToXArg(arg)
        xp.instrList.add(XMovq(sentArgA, dest))
        xp.instrList.add(XAddq(sentArgB, dest))
    }
}

class CRead: CExpr {
    override fun select(xp: XProgram, arg: Argument) {
        xp.instrList.add(XCallq(XLabel("_read")))
        xp.instrList.add(XMovq(XReg("rax"), convertCArgToXArg(arg)))
    }
}

data class CWrite(private var a: Argument): CExpr {
    override fun select(xp: XProgram, arg: Argument) {
        val sentArg = convertCArgToXArg(a)
        xp.instrList.add(XPushq(sentArg))
        xp.instrList.add(XCallq(XLabel("_print")))
    }
}

/**
 * Converts a generic CArg to a generic XArg.
 * @param arg: CArgument to convert.
 * @return Converted XArg.
 */
fun convertCArgToXArg(arg: Argument): XArg {
    return when(arg.getVal()) {
        is Int -> {
            val aVal = arg.getVal() as Int
            XInt(aVal)
        }
        is String -> {
            val aVal = arg.getVal() as String
            XVar(aVal)
        }
        else -> {
            throw RuntimeException("Incompatible type")
        }
    }
}

/**
 * A full C0 program.
 */
data class CProgram(val varList: HashSet<String>, val stmtList: MutableList<CStmt>, var arg: Argument) {
    fun select(): XProgram {
        val xp = XProgram(mutableListOf(), mutableListOf())
        varList.forEach { xp.varList.add(XVar(it)) }
        stmtList.forEach {
            it.xe.select(xp, it.x)
        }
        return xp
    }
}
