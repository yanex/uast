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
package org.jetbrains.uast.java

import com.intellij.psi.PsiNewExpression
import org.jetbrains.uast.UClassNotResolved
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UObjectLiteralExpression
import org.jetbrains.uast.psi.PsiElementBacked

class JavaUObjectLiteralExpression(
        override val psi: PsiNewExpression,
        override val parent: UElement?
) : JavaAbstractUExpression(), UObjectLiteralExpression, PsiElementBacked {
    override val declaration by lz {
        psi.anonymousClass?.let { JavaUClass(it, this, psi) } ?: UClassNotResolved
    }
}