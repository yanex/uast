package org.jetbrains.uast.evaluation

import com.intellij.psi.PsiEnumConstant
import com.intellij.psi.PsiVariable
import org.jetbrains.uast.*
import org.jetbrains.uast.expressions.UReferenceExpression
import org.jetbrains.uast.values.UValue
import org.jetbrains.uast.visitor.UastTypedVisitor

class TreeBasedEvaluator(
        private val context: UastContext
) : UastTypedVisitor<UEvaluationState, UEvaluationInfo>, UEvaluator {

    override fun visitElement(node: UElement, data: UEvaluationState) = UEvaluationInfo(UValue.Undetermined, data)

    override fun analyze(method: UMethod, state: UEvaluationState) {
        method.uastBody?.accept(this, state)
    }

    override fun evaluate(expression: UExpression, state: UEvaluationState) = expression.accept(this, state).value

    // ----------------------- //

    private infix fun UValue.to(state: UEvaluationState) = UEvaluationInfo(this, state)

    override fun visitLiteralExpression(node: ULiteralExpression, data: UEvaluationState): UEvaluationInfo {
        val value = node.value
        return when (value) {
            null -> UValue.Null
            is Number -> value.let {
                if (it is Float || it is Double) UValue.NumericFloat(value.toDouble())
                else UValue.NumericInt(value.toLong())
            }
            is Char -> UValue.NumericInt(value.toLong(), 2)
            is Boolean -> UValue.Bool(value)
            is String -> UValue.Undetermined
            else -> UValue.Undetermined
        } to data
    }

    override fun visitClassLiteralExpression(node: UClassLiteralExpression, data: UEvaluationState) =
            (node.type?.let { UValue.ClassLiteral(it) } ?: UValue.Undetermined) to data

    override fun visitReturnExpression(node: UReturnExpression, data: UEvaluationState) =
            UValue.Nothing to data

    override fun visitBreakExpression(node: UBreakExpression, data: UEvaluationState) =
            UValue.Nothing to data

    override fun visitContinueExpression(node: UContinueExpression, data: UEvaluationState) =
            UValue.Nothing to data

    override fun visitThrowExpression(node: UThrowExpression, data: UEvaluationState) =
            UValue.Nothing to data

    // ----------------------- //

    override fun visitSimpleNameReferenceExpression(
            node: USimpleNameReferenceExpression,
            data: UEvaluationState
    ): UEvaluationInfo {
        val resolvedElement = node.resolve()
        return when (resolvedElement) {
            is PsiEnumConstant -> UValue.EnumEntry(resolvedElement)
            is PsiVariable -> data[context.getVariable(resolvedElement)]
            else -> return visitReferenceExpression(node, data)
        } to data
    }

    override fun visitReferenceExpression(
            node: UReferenceExpression,
            data: UEvaluationState
    ) = UValue.External(node) to data

    // ----------------------- //

    private fun UExpression.assign(value: UExpression, data: UEvaluationState): UEvaluationInfo {
        val valueInfo = value.accept(this@TreeBasedEvaluator, data)
        if (this is UResolvable) {
            val resolvedElement = resolve()
            if (resolvedElement is PsiVariable) {
                val variable = context.getVariable(resolvedElement)
                return UValue.Undetermined to valueInfo.state.assign(variable, valueInfo.value, this)
            }
        }
        return UValue.Undetermined to valueInfo.state
    }

    override fun visitBinaryExpression(node: UBinaryExpression, data: UEvaluationState): UEvaluationInfo {
        if (node.operator == UastBinaryOperator.ASSIGN) return node.leftOperand.assign(node.rightOperand, data)
        val leftInfo = node.leftOperand.accept(this, data)
        if (leftInfo.value == UValue.Nothing) return leftInfo
        val rightInfo = node.rightOperand.accept(this, leftInfo.state)
        return when (node.operator) {
            UastBinaryOperator.PLUS -> leftInfo.value + rightInfo.value
            UastBinaryOperator.MINUS -> leftInfo.value - rightInfo.value
            else -> UValue.Undetermined
        } to rightInfo.state
    }

    // ----------------------- //

    override fun visitIfExpression(node: UIfExpression, data: UEvaluationState): UEvaluationInfo {
        val conditionInfo = node.condition.accept(this, data)
        val thenInfo = node.thenExpression?.accept(this, conditionInfo.state)
        val elseInfo = node.elseExpression?.accept(this, conditionInfo.state)
        val conditionValue = conditionInfo.value
        val defaultInfo = UValue.Undetermined to conditionInfo.state
        return when (conditionValue) {
            is UValue.Bool -> {
                if (conditionValue.value) thenInfo ?: defaultInfo
                else elseInfo ?: defaultInfo
            }
            else -> {
                if (thenInfo == null) elseInfo?.merge(defaultInfo) ?: defaultInfo
                else if (elseInfo == null) thenInfo.merge(defaultInfo)
                else thenInfo.merge(elseInfo)
            }
        }
    }
}