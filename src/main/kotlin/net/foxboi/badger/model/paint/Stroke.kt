package net.foxboi.badger.model.paint

import net.foxboi.badger.graphics.Cap
import net.foxboi.badger.graphics.Join
import net.foxboi.badger.model.dyn.Dyn
import net.foxboi.badger.util.Color
import net.foxboi.badger.util.Length

class Stroke(
    val color: Dyn<Color>,
    val width: Dyn<Length>,
    val join: Dyn<Join> = Dyn.const(Join.MITER),
    val cap: Dyn<Cap> = Dyn.const(Cap.FLAT),
    val miterLimit: Dyn<Double> = Dyn.const(2.0)
) : Paint