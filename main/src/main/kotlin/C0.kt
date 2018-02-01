interface Argument {
    fun getVal(): Any
}
interface CExpr

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

data class CLet(val a: Argument): CExpr

data class CStmt(var x: CVar, val xe: CExpr)

data class CNeg(val a: Argument): CExpr

data class CAdd(val a: Argument, val b: Argument): CExpr

class CRead: CExpr
data class CWrite(var a: Argument): CExpr

data class CProgram(val varList: MutableList<String>, val stmtList: MutableList<CStmt>, var arg: Argument)
