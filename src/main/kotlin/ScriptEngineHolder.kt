package main.kotlin;

import javax.script.*

object ScriptEngineHolder {
    val kts: ScriptEngine? by lazy { ScriptEngineManager().getEngineByExtension("kts")?.apply { eval("import arc.*;import mindustry.*;import main.*;") } }
}