/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.uast

import org.jetbrains.uast.internal.log
import org.jetbrains.uast.visitor.UastVisitor

/**
 * Represents
 *
 * `if (condition) {
 *     // do if true
 * } else {
 *     // do if false
 * }`
 *
 * and
 *
 * `condition : trueExpression ? falseExpression`
 *
 * condition expressions.
 */
interface UIfExpression : UExpression {
    /**
     * Returns the condition expression.
     */
    val condition: UExpression

    /**
     * Returns the expression which is executed if the condition is true, or null if the expression is empty.
     */
    val thenExpression: UExpression?

    /**
     * Returns the expression which is executed if the condition is false, or null if the expression is empty.
     */
    val elseExpression: UExpression?

    /**
     * Returns true if the expression is ternary (condition ? trueExpression : falseExpression).
     */
    val isTernary: Boolean

    override fun accept(visitor: UastVisitor) {
        if (visitor.visitIfExpression(this)) return
        condition.accept(visitor)
        thenExpression?.accept(visitor)
        elseExpression?.accept(visitor)
        visitor.afterVisitIfExpression(this)
    }

    override fun logString() = log("UIfExpression", condition, thenExpression, elseExpression)

    override fun renderString() = buildString {
        if (isTernary) {
            append("(" + condition.renderString() + ")")
            append(" ? ")
            append("(" + (thenExpression?.renderString() ?: "<noexpr>") + ")")
            append(" : ")
            append("(" + (elseExpression?.renderString() ?: "<noexpr>") + ")")
        } else {
            append("if (${condition.renderString()}) ")
            thenExpression?.let { append(it.renderString()) }
            val elseBranch = elseExpression
            if (elseBranch != null && elseBranch !is UastEmptyExpression) {
                if (thenExpression !is UBlockExpression) append(" ")
                append("else ")
                append(elseBranch.renderString())
            }
        }
    }
}