data class Num(private val n: Int) : Expr {
    override fun flatten(count: Counter): CProgram {
        return CProgram(mutableListOf(), mutableListOf(), CNum(n))
    }

    override fun uniquify(env: HashMap<String, String>, count: Counter) {
        // No need to do anything
    }

    override fun eval(env: HashMap<String, Int>): Int {
        return n
    }
}

data class Neg(private val e: Expr) : Expr {
    override fun flatten(count: Counter): CProgram {
        val cP = e.flatten(count)
        val newVal = "neg_${count.count}"
        count.count += 1
        cP.varList.add(newVal)
        cP.stmtList.add(CStmt(CVar(newVal), CNeg(cP.arg)))
        cP.arg = CVar(newVal)
        return cP
    }

    override fun uniquify(env: HashMap<String, String>, count: Counter) {
        e.uniquify(env, count)
    }

    override fun eval(env: HashMap<String, Int>): Int {
        return e.eval(env) * -1
    }
}

data class Add(private val l: Expr, private val r: Expr) : Expr {
    override fun flatten(count: Counter): CProgram {
        val cPL = l.flatten(count)
        val cPR = r.flatten(count)

        val newVal = "add_${count.count}"
        count.count += 1

        val varList = mutableListOf<String>()
        varList.addAll(cPL.varList)
        varList.addAll(cPR.varList)
        varList.add(newVal)

        val stmtList = mutableListOf<CStmt>()
        stmtList.addAll(cPR.stmtList)
        stmtList.addAll(cPL.stmtList)
        stmtList.add(CStmt(CVar(newVal), CAdd(cPL.arg, cPR.arg)))

        return CProgram(varList, stmtList, CVar(newVal))
    }

    override fun uniquify(env: HashMap<String, String>, count: Counter) {
        l.uniquify(env, count)
        r.uniquify(env, count)
    }

    override fun eval(env: HashMap<String, Int>): Int {
        return l.eval(env) + r.eval(env)
    }
}

data class Let(private var x: String, private val xe: Expr, private val be: Expr) : Expr {
    override fun flatten(count: Counter): CProgram {
        val cPXE = xe.flatten(count)
        val cpBE = be.flatten(count)

        val varList = mutableListOf<String>()
        varList.addAll(cPXE.varList)
        varList.addAll(cpBE.varList)
        varList.add(x)

        val stmtList = mutableListOf<CStmt>()
        stmtList.addAll(cPXE.stmtList)
        stmtList.add(CStmt(CVar(x), CLet(cPXE.arg)))
        stmtList.addAll(cpBE.stmtList)

        return CProgram(varList, stmtList, cpBE.arg)
    }

    override fun uniquify(env: HashMap<String, String>, count: Counter) {
        val newName = "${x}_${count.count}"
        count.count = count.count + 1
        env[x] = newName
        x = newName

        xe.uniquify(env, count)
        be.uniquify(env, count)
    }

    override fun eval(env: HashMap<String, Int>): Int {
        env[x] = xe.eval(env)
        return be.eval(env)
    }
}

data class Var(private var x: String) : Expr {
    override fun flatten(count: Counter): CProgram {
        val varList = mutableListOf<String>()
        varList.add(x)
        return CProgram(varList, mutableListOf(), CVar(x))
    }

    override fun uniquify(env: HashMap<String, String>, count: Counter) {
        val newName = env[x]
        newName?.let {
            x = newName
            return
        }
        throw RuntimeException("Syntax error, variable $x not defined before use")
    }

    override fun eval(env: HashMap<String, Int>): Int {
        val value = env[x]

        try {
            return value as Int
        } catch (e: ClassCastException) {
            throw RuntimeException("Syntax error, variable $x not defined before use")
        }
    }
}

class Read : Expr {
    override fun flatten(count: Counter): CProgram {
        val newVal = "rv_${count.count}"
        count.count += 1
        return CProgram(mutableListOf(newVal), mutableListOf(CStmt(CVar(newVal), CRead())), CVar(newVal))
    }

    override fun uniquify(env: HashMap<String, String>, count: Counter) {
        // Do not need to do anything
    }

    override fun eval(env: HashMap<String, Int>): Int {
        val input = readLine()
        input?.let {
            return input.toInt()
        }
        throw NumberFormatException("Invalid input in read.")
    }
}

class Write(private var x: Var) : Expr {
    override fun flatten(count: Counter): CProgram {
        val cP = x.flatten(count)
        cP.stmtList.add(CStmt(CVar(cP.arg.getVal() as String), CWrite(cP.arg)))
        return cP
    }

    override fun uniquify(env: HashMap<String, String>, count: Counter) {
            x.uniquify(env, count)
            return
    }

    override fun eval(env: HashMap<String, Int>): Int {
        val value = x.eval(env)
        println(value)
        return value
    }

}

interface Expr {
    fun eval(env: HashMap<String, Int>): Int
    fun uniquify(env: HashMap<String, String>, count: Counter)
    fun flatten(count: Counter): CProgram
}

data class Program(val e: Expr) {
    fun uniquify() {
        e.uniquify(hashMapOf(), Counter(0))
    }

    fun flatten(): CProgram {
        return e.flatten(Counter(0))
    }
}

data class Counter(var count: Int) {
    override fun toString(): String {
        return "$count"
    }
}

