interface Argument {
    fun getVal(): Any
}
interface CExpr {
    fun select(xp: XProgram, arg: Argument)
}

data class CNum(private var n: Int = 314159): Argument {
    override fun getVal(): Any {
        return n
    }

}
data class CVar(private var x: String): Argument {
    override fun getVal(): Any {
        return x
    }
}

data class CStmt(var x: CVar, val xe: CExpr)

data class CLet(val a: Argument): CExpr {
    override fun select(xp: XProgram, arg: Argument) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

data class CNeg(val a: Argument): CExpr {
    override fun select(xp: XProgram, arg: Argument) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

data class CAdd(val a: Argument, val b: Argument): CExpr {
    override fun select(xp: XProgram, arg: Argument) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

class CRead: CExpr {
    override fun select(xp: XProgram, arg: Argument) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

data class CWrite(var a: Argument): CExpr {
    override fun select(xp: XProgram, arg: Argument) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

data class CProgram(val varList: MutableList<String>, val stmtList: MutableList<CStmt>, var arg: Argument)
