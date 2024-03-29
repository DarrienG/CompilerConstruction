interface Argument {
    fun getVal(): Any
}
interface CExpr {
    fun select(xp: XProgram, arg: Argument)
}

data class CType(private val t: Type): Argument {
    override fun getVal(): Any {
        return t
    }

}

data class CNum(private val n: Int = 314159): Argument {
    override fun getVal(): Any {
        return n
    }
}

data class CVar(private val x: String): Argument {
    override fun getVal(): Any {
        return x
    }
}

data class CStmt(val x: CVar, val xe: CExpr)

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

data class CNot(private val a: Argument): CExpr {
    override fun select(xp: XProgram, arg: Argument) {
        val sentArg = convertCArgToXArg(a)
        xp.instrList.add(XOrq(sentArg, XInt(1)))
    }
}

data class CAnd(private val a: Argument, private val b: Argument): CExpr {
    override fun select(xp: XProgram, arg: Argument) {
        val sentArgA = convertCArgToXArg(a)
        val sentArgB = convertCArgToXArg(b)
        val dest = convertCArgToXArg(arg)
        xp.instrList.add(XMovq(sentArgA, dest))
        xp.instrList.add(XAnd(sentArgB, dest))
    }
}

data class COr(private val a: Argument, private  val b: Argument): CExpr {
    override fun select(xp: XProgram, arg: Argument) {
        val sentArgA = convertCArgToXArg(a)
        val sentArgB = convertCArgToXArg(b)
        val dest = convertCArgToXArg(arg)
        xp.instrList.add(XMovq(sentArgA, dest))
        xp.instrList.add(XOrq(sentArgB, dest))
    }
}

data class CComp(private val a: Argument, private val b: Argument, val type: CmpType): CExpr {
    override fun select(xp: XProgram, arg: Argument) {
        val sentArgA = convertCArgToXArg(a)
        val sentArgB = convertCArgToXArg(b)
        val dest = convertCArgToXArg(arg)

        xp.instrList.add(XMovq(sentArgA, dest))
        when(type) {
            // These need a little extra love and care
            CmpType.OR -> {
                xp.instrList.add(XAddq(sentArgB, dest))
                xp.instrList.add(XCmpq(XInt(0), dest))
            }
            CmpType.NOT -> {
                xp.instrList.add(XXOrq(sentArgA, sentArgB))
                xp.instrList.add(XCmpq(XInt(1), dest))
            }
            else -> xp.instrList.add(XCmpq(sentArgB, dest))
        }
    }
}

data class CIf(private val labs: IfLabs, private val cc: CComp, private val tList: List<CStmt>, private val fList: List<CStmt>): CExpr {
    override fun select(xp: XProgram, arg: Argument) {
        xp.instrList.add(XJmpIf(cc.type, labs.trueL))
        xp.instrList.add(XJmp(labs.falseL))

        xp.instrList.add(labs.trueL.invoke())

        tList.forEach { it.xe.select(xp, it.x) }
        xp.instrList.add(XJmp(labs.endL))

        xp.instrList.add(labs.falseL.invoke())
        fList.forEach { it.xe.select(xp, it.x) }
        xp.instrList.add(XJmp(labs.endL))

        xp.instrList.add(labs.endL.invoke())
    }
}

data class CMalloc(private val a: Argument): CExpr {
    override fun select(xp: XProgram, arg: Argument) {
        xp.instrList.addAll(XCallq(XLabel("gc_malloc")).emitFullInstrSet())
    }
}

data class CVecRef(private val a: Argument): CExpr {
    // TODO: This whole thing is wrong
    override fun select(xp: XProgram, arg: Argument) {
        // TODO: This is wrong - a is currently a type and should be something else
        // Technically they should both be movq, but the arguments are fucked up
        xp.instrList.add(XMovq(convertCArgToXArg(a), XReg("rax")))
        xp.instrList.add(XMovq(XOffset(XReg("rax"), 8 * arg.getVal() as Int), XReg("rax")))
    }
}

data class CVecSet(private val value: Argument, private val idx: Argument): CExpr {
    override fun select(xp: XProgram, arg: Argument) {
        xp.instrList.add(XMovq(convertCArgToXArg(value), XOffset(XReg("rax"), 8 * idx.getVal() as Int)))
        xp.instrList.add(XMovq(convertCArgToXArg(value), XReg("rax")))
        // r15???????????????? What the fuck
        xp.instrList.add(XMovq(XReg("r15"), XOffset(XReg("rax"), 8 * idx.getVal() as Int)))
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
        xp.instrList.addAll(XCallq(XLabel("_read")).emitFullInstrSet())
        xp.instrList.add(XMovq(XReg("rax"), convertCArgToXArg(arg)))
    }
}

data class CWrite(private val a: Argument): CExpr {
    override fun select(xp: XProgram, arg: Argument) {
        val sentArg = convertCArgToXArg(a)
        xp.instrList.add(XMovq(sentArg, XReg("rdi")))
        xp.instrList.addAll(XCallq(XLabel("_print")).emitFullInstrSet())
    }
}

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

data class CProgram(val varList: HashSet<String>, val stmtList: MutableList<CStmt>, var arg: Argument) {
    fun select(): XProgram {
        val xp = XProgram(mutableListOf(), mutableListOf())
        varList.forEach { xp.varList.add(XVar(it)) }
        stmtList.forEach { it.xe.select(xp, it.x) }
        xp.instrList.add(XMovq(convertCArgToXArg(arg), XReg("rax")))
        return xp
    }
}
