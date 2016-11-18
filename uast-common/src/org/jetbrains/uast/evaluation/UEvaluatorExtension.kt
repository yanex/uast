package org.jetbrains.uast.evaluation

import com.intellij.lang.Language
import com.intellij.openapi.extensions.ExtensionPointName
import org.jetbrains.uast.UastBinaryOperator
import org.jetbrains.uast.UastPostfixOperator
import org.jetbrains.uast.UastPrefixOperator
import org.jetbrains.uast.UastQualifiedExpressionAccessType
import org.jetbrains.uast.values.UValue

interface UEvaluatorExtension {

    companion object {
        val EXTENSION_POINT_NAME: ExtensionPointName<UEvaluatorExtension> =
                ExtensionPointName.create<UEvaluatorExtension>("org.jetbrains.uast.evaluation.UEvaluatorExtension")
    }

    infix fun UValue.to(state: UEvaluationState) = UEvaluationInfo(this, state)

    val language: Language

    fun evaluatePostfix(
            operator: UastPostfixOperator,
            operandValue: UValue,
            state: UEvaluationState
    ): UEvaluationInfo = UValue.Undetermined to state

    fun evaluatePrefix(
            operator: UastPrefixOperator,
            operandValue: UValue,
            state: UEvaluationState
    ): UEvaluationInfo = UValue.Undetermined to state

    fun evaluateBinary(
            operator: UastBinaryOperator,
            leftValue: UValue,
            rightValue: UValue,
            state: UEvaluationState
    ): UEvaluationInfo = UValue.Undetermined to state

    fun evaluateQualified(
            accessType: UastQualifiedExpressionAccessType,
            receiverInfo: UEvaluationInfo,
            selectorInfo: UEvaluationInfo
    ): UEvaluationInfo = UValue.Undetermined to selectorInfo.state
}