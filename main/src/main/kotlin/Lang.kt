enum class Type {
    INT,
    BOOL;
}

enum class CmpType {
    EQ,     // Equals, and tunes your music
    NE,     // Not equals
    GT,     // Greater than
    LT,     // Less than
    GTE,    // Greater than equal to
    LTE,    // 5G, less than equal to
    ZER,    // Is zero
    NZER,   // Not zero
    AND,   // Not zero
    OR,   // Not zero
    NOT;   // Not zero
}

data class Bool(private val b: String): Comp(CmpType.AND, Num(1), Num(if (b == "t") 1 else 0)) {
    private val t: Int = if (b == "t") 1 else 0

    override fun eval(env: HashMap<String, VarPair>): Int {
        return t
    }
}

data class Num(private val n: Int) : Expr {
    override fun getType(): Type {
        return Type.INT
    }

    override fun flatten(count: Counter): CProgram {
        return CProgram(hashSetOf(), mutableListOf(), CNum(n))
    }

    override fun uniquify(env: HashMap<String, String>, count: Counter) {
        // No need to do anything
    }

    override fun eval(env: HashMap<String, VarPair>): Int {
        return n
    }
}

data class Neg(private val e: Expr): Expr {
    override fun getType(): Type {
        return Type.INT
    }

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

    override fun eval(env: HashMap<String, VarPair>): Int {
        if (e.getType() != Type.INT) throw RuntimeException("Attempting to negate non-int")
        return e.eval(env) * -1
    }
}

data class Add(private val l: Expr, private val r: Expr): Expr {
    override fun getType(): Type {
        return Type.INT
    }

    override fun flatten(count: Counter): CProgram {
        val cPL = l.flatten(count)
        val cPR = r.flatten(count)

        val newVal = "add_${count.count}"
        count.count += 1

        val varList = hashSetOf<String>()
        varList.addAll(cPL.varList)
        varList.addAll(cPR.varList)
        varList.add(newVal)

        val stmtList = mutableListOf<CStmt>()
        stmtList.addAll(cPL.stmtList)
        stmtList.addAll(cPR.stmtList)
        stmtList.add(CStmt(CVar(newVal), CAdd(cPL.arg, cPR.arg)))

        return CProgram(varList, stmtList, CVar(newVal))
    }

    override fun uniquify(env: HashMap<String, String>, count: Counter) {
        l.uniquify(env, count)
        r.uniquify(env, count)
    }

    override fun eval(env: HashMap<String, VarPair>): Int {
        weNumero(l, r)
        return l.eval(env) + r.eval(env)
    }
}

data class Let(private var x: String, private val xe: Expr, private val be: Expr) : Expr {
    override fun getType(): Type {
        return xe.getType()
    }

    override fun flatten(count: Counter): CProgram {
        val cPXE = xe.flatten(count)
        val cpBE = be.flatten(count)

        val varList = hashSetOf<String>()
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

    override fun eval(env: HashMap<String, VarPair>): Int {
        env[x] = VarPair(xe.getType(), xe.eval(env))
        return be.eval(env)
    }
}

data class If(private val cc: Comp, private val trX: Expr, private val faX: Expr): Expr {
    override fun getType(): Type {
        val rT = trX.getType()
        val lT = faX.getType()
        if (rT != lT) {
            throw RuntimeException("Incompatible expression types")
        }
        return rT
    }

    override fun eval(env: HashMap<String, VarPair>): Int {
        val t = cc.getType()
        if (t != Type.BOOL) {
            throw RuntimeException("Attempting to evaluate non-bool type: $t")
        }
        getType()

        return if (cc.eval(env) != 0) {
            trX.eval(env)
        } else {
            faX.eval(env)
        }
    }

    override fun uniquify(env: HashMap<String, String>, count: Counter) {
        cc.uniquify(env, count)
        trX.uniquify(env, count)
        faX.uniquify(env, count)
    }

    override fun flatten(count: Counter): CProgram {
        val cCon = cc.flatten(count)
        val cPT = trX.flatten(count)
        val cPF = faX.flatten(count)

        val startLabel = "if_${count.count}"
        val trueLabel = "tif_${count.count}"
        val falseLabel = "fif_${count.count}"
        count.count += 1

        val varList = hashSetOf<String>()
        varList.addAll(cCon.varList)
        varList.addAll(cPT.varList)
        varList.addAll(cPF.varList)

        val sl = XLabel(startLabel)
        val tl = XLabel(trueLabel)
        val fl = XLabel(falseLabel)
        val el = XLabel("${startLabel}_end")

        varList.add(sl.toString())
        varList.add(tl.toString())
        varList.add(fl.toString())
        varList.add(el.toString())

        val stmtList = mutableListOf<CStmt>()
        stmtList.addAll(cCon.stmtList)

        cPT.stmtList.add(CStmt(CVar(trueLabel), CLet(cPT.arg)))
        cPF.stmtList.add(CStmt(CVar(falseLabel), CLet(cPF.arg)))

        val labs = IfLabs(
                sl,
                tl,
                fl,
                el
        )

        // This statement is about as flat as the Earth
        // You decide what that means
        stmtList.add(CStmt(CVar(startLabel),
                CIf(labs, CComp(CNum(1), cCon.arg, cc.type),
                        cPT.stmtList, cPF.stmtList)))

        return CProgram(varList, stmtList, CVar(startLabel))
    }
}

data class IfLabs(val startL: XLabel, val trueL: XLabel, val falseL: XLabel, val endL: XLabel)

data class And(private val lx: Expr, private val rx: Expr): Comp(CmpType.AND, lx, rx)

data class Or(private val lx: Expr, private val rx: Expr): Comp(CmpType.OR, lx, rx)

data class Not(private val e: Expr): Comp(CmpType.NOT, e, Num(1))

open class Comp(val type: CmpType, private val lx: Expr, private val rx: Expr): Expr {
    override fun getType(): Type {
        return Type.BOOL
    }

    override fun eval(env: HashMap<String, VarPair>): Int {
        return when(type) {
            CmpType.EQ -> {
                weSame(lx, rx)
                if (lx.eval(env) == rx.eval(env)) 1 else 0
            }
            CmpType.NE -> {
                weSame(lx, rx)
                if (lx.eval(env) != rx.eval(env)) 1 else 0
            }
            CmpType.GT -> {
                weNumero(lx, rx)
                if (lx.eval(env) > rx.eval(env)) 1 else 0
            }
            CmpType.LT -> {
                weNumero(lx, rx)
                if (lx.eval(env) < rx.eval(env)) 1 else 0
            }
            CmpType.GTE -> {
                weNumero(lx, rx)
                if (lx.eval(env) >= rx.eval(env)) 1 else 0
            }
            CmpType.LTE -> {
                weNumero(lx, rx)
                if (lx.eval(env) <= rx.eval(env)) 1 else 0
            }
            CmpType.ZER -> {
                weNumero(lx, rx)
                if (lx.eval(env) + rx.eval(env) == 0) 1 else 0
            }
            CmpType.NZER -> {
                weNumero(lx, rx)
                if (lx.eval(env) + rx.eval(env) != 0) 1 else 0
            }
            CmpType.AND -> {
                weBoolin(lx, rx)
                if (lx.eval(env) + rx.eval(env) != 2) 1 else 0
            }
            CmpType.OR -> {
                weBoolin(lx, rx)
                if (lx.eval(env) + rx.eval(env) > 0) 1 else 0
            }
            CmpType.NOT -> {
                if (lx.getType() != Type.BOOL) throw RuntimeException("Attempting to <NOT> a non-bool type")
                lx.eval(env).xor(rx.eval(env))
            }
        }
    }

    override fun uniquify(env: HashMap<String, String>, count: Counter) {
        lx.uniquify(env, count)
        rx.uniquify(env, count)
    }

    override fun flatten(count: Counter): CProgram {
        val cPL = lx.flatten(count)
        val cPR = rx.flatten(count)

        val newVal = "comp_${type.name}_${count.count}"
        count.count += 1

        val varList = hashSetOf<String>()
        varList.addAll(cPL.varList)
        varList.addAll(cPR.varList)
        varList.add(newVal)

        val stmtList = mutableListOf<CStmt>()
        stmtList.addAll(cPL.stmtList)
        stmtList.addAll(cPR.stmtList)
        stmtList.add(CStmt(CVar(newVal), CComp(cPL.arg, cPR.arg, type)))

        return CProgram(varList, stmtList, CVar(newVal))
    }
}


data class Var(private val t: Type, private var x: String) : Expr {
    override fun getType(): Type {
        return t
    }

    override fun flatten(count: Counter): CProgram {
        val varList = hashSetOf<String>()
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

    override fun eval(env: HashMap<String, VarPair>): Int {
        val value = env[x]
        value?.let {
            if (it.t != t) throw RuntimeException("Using incompatible type with variable")

            try {
                return value.v
            } catch (e: ClassCastException) {
                throw RuntimeException("Syntax error, variable $x not defined before use")
            }
        }

        throw RuntimeException("Syntax error, variable $x not defined before use")
    }
}

class Read(private val t: Type): Expr {
    override fun getType(): Type {
        return t
    }

    override fun flatten(count: Counter): CProgram {
        val newVal = "rv_${count.count}"
        count.count += 1
        return CProgram(hashSetOf(newVal), mutableListOf(CStmt(CVar(newVal), CRead())), CVar(newVal))
    }

    override fun uniquify(env: HashMap<String, String>, count: Counter) {
        // Do not need to do anything
    }

    override fun eval(env: HashMap<String, VarPair>): Int {
        val input = readLine()
        input?.let {
            return input.toInt()
        }
        throw NumberFormatException("Invalid input in read.")
    }
}

class Write(private var x: Expr) : Expr {
    override fun getType(): Type {
        return x.getType()
    }

    override fun flatten(count: Counter): CProgram {
        val cP = x.flatten(count)
        cP.stmtList.add(CStmt(CVar(cP.arg.getVal() as String), CWrite(cP.arg)))
        return cP
    }

    override fun uniquify(env: HashMap<String, String>, count: Counter) {
        x.uniquify(env, count)
        return
    }

    override fun eval(env: HashMap<String, VarPair>): Int {
        val value = x.eval(env)
        println(value)
        return value
    }

    override fun toString(): String {
        return x.toString()
    }
}

data class VarPair(val t: Type, val v: Int)

interface Expr {
    fun eval(env: HashMap<String, VarPair>): Int
    fun uniquify(env: HashMap<String, String>, count: Counter)
    fun flatten(count: Counter): CProgram
    fun getType(): Type
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

/**
 * Checks to see if two expressions booleans.
 */
fun weBoolin(lx: Expr, rx: Expr) {
    if (lx.getType() != Type.BOOL && rx.getType() != Type.BOOL) {
        throw RuntimeException("Attempting to compare two non-bool types")
    }
}

/**
 * Checks to see if two expressions are numbers.
 */
fun weNumero(lx: Expr, rx: Expr) {
    if (lx.getType() != Type.INT && rx.getType() != Type.INT) {
        throw RuntimeException("Attempting to perform int operation on two non ints")
    }
}

fun weSame(lx: Expr, rx: Expr) {
    if (lx.getType() != rx.getType()) {
        throw RuntimeException("Attempting to perform operation on two incompatible types")
    }
}

